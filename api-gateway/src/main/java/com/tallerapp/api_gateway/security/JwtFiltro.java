package com.tallerapp.api_gateway.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Toda peticion pasa por aca antes de llegar a los servicios.
 *
 * Como los servicios estan detras del gateway y no se exponen al navegador,
 * alcanza con validar el token en este unico lugar. Si el token es valido,
 * el usuario y su rol viajan hacia el servicio en cabeceras.
 */
@Component
@Order(1)
public class JwtFiltro extends OncePerRequestFilter {
	
	@Value("${jwt.secreto}")
	private String secreto;
	
	// Lo unico que se puede usar sin haber entrado: el login.
	// OPTIONS tambien pasa, porque es el pedido previo que hace el navegador
	// para chequear los permisos y viaja sin cabeceras propias.
	private static final List<String> LIBRES = List.of("/auth/login");
	
	public static final String CABECERA_ID = "X-Empleado-Id";
	public static final String CABECERA_ROL = "X-Empleado-Rol";
	
	// Solo un ADMIN puede entrar aca, ni siquiera para mirar:
	// son los datos fiscales del taller.
	private static final List<String> SOLO_ADMIN = List.of("/configuracion");
	
	// La excepcion a lo de arriba. El que atiende el mostrador tiene que
	// poder imprimir un recibo con membrete y dictarle el CBU al cliente
	// sin ser administrador. Leerlo no es secreto —el CBU se le muestra al
	// cliente igual—; lo delicado es cambiarlo, y eso sigue siendo de ADMIN.
	private static final List<String> EXCEPCIONES_SOLO_ADMIN = List.of("/configuracion/cobro");
	
	// Aca cualquiera puede mirar pero solo un ADMIN puede tocar.
	//
	// Un mecanico necesita ver la lista de empleados y sucursales para
	// cargar una orden, y el catalogo del deposito para cargar repuestos
	// en una orden. Lo que no hace es dar de alta gente ni cambiar precios:
	// el que puede tocar un precio puede regalar el trabajo.
	//
	// Ojo: los movimientos de stock (/movimientos) quedan afuera a
	// proposito. Mover stock es operar el taller, no administrarlo.
	private static final List<String> ADMIN_PARA_ESCRIBIR = List.of(
			"/empleados", "/sucursales", "/auth/password", "/repuestos");
	
	// Deshacer plata es de administrador: el que puede anular un cobro
	// puede hacer desaparecer una venta del arqueo del dia.
	private static final List<String> SOLO_ADMIN_ANULAR = List.of("/pagos/anular", "/ventas/anular");
	
	// El historial de precios muestra a cuanto se compro cada repuesto,
	// que es el margen del taller.
	private static final List<String> SOLO_ADMIN_PRECIOS = List.of("/repuestos/precios");
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
			throws ServletException, IOException {
		
		String ruta = peticion.getRequestURI();
		
		if ("OPTIONS".equalsIgnoreCase(peticion.getMethod()) || this.esLibre(ruta)) {
			cadena.doFilter(peticion, respuesta);
			return;
		}
		
		String token = this.leerToken(peticion);
		
		if (token == null) {
			this.cortar(respuesta, HttpStatus.UNAUTHORIZED, "Necesitas iniciar sesion");
			return;
		}
		
		Claims datos;
		
		try {
			datos = Jwts.parser()
					.verifyWith(this.clave())
					.build()
					.parseSignedClaims(token)
					.getPayload();
			
		} catch (Exception e) {
			// vencido, adulterado o firmado con otro secreto
			this.cortar(respuesta, HttpStatus.UNAUTHORIZED, "Tu sesion venció, volvé a entrar");
			return;
		}
		
		String rol = String.valueOf(datos.get("rol"));
		
		boolean esAdmin = "ADMIN".equals(rol);
		
		if (this.esSoloAdmin(ruta) && !esAdmin) {
			this.cortar(respuesta, HttpStatus.FORBIDDEN, "Esta seccion es solo para administradores");
			return;
		}
		
		if (this.esAdminParaEscribir(ruta) && !esAdmin && !"GET".equalsIgnoreCase(peticion.getMethod())) {
			this.cortar(respuesta, HttpStatus.FORBIDDEN, "Solo un administrador puede hacer este cambio");
			return;
		}
		
		// Borrar es de administrador en todo el sistema. Es una sola regla
		// y se explica en una linea: el que atiende carga y corrige, el que
		// administra es el unico que hace desaparecer cosas.
		if ("DELETE".equalsIgnoreCase(peticion.getMethod()) && !esAdmin) {
			this.cortar(respuesta, HttpStatus.FORBIDDEN, "Solo un administrador puede borrar");
			return;
		}
		
		if (this.empieza(SOLO_ADMIN_ANULAR, ruta) && !esAdmin) {
			this.cortar(respuesta, HttpStatus.FORBIDDEN, "Solo un administrador puede anular un cobro o una venta");
			return;
		}
		
		if (this.empieza(SOLO_ADMIN_PRECIOS, ruta) && !esAdmin) {
			this.cortar(respuesta, HttpStatus.FORBIDDEN, "El historial de precios es solo para administradores");
			return;
		}
		
		// El servicio de atras recibe quien es el usuario en dos cabeceras que
		// agregamos aca. Asi el navegador no puede mentir sobre quien hizo que:
		// el dato sale del token, no de lo que mande el front.
		String idEmpleado = String.valueOf(datos.get("id_empleado"));
		
		cadena.doFilter(this.conUsuario(peticion, idEmpleado, rol), respuesta);
	}
	
	// Envuelve la peticion para que lleve el usuario en cabeceras.
	// Se sobreescriben los tres metodos de lectura de cabeceras porque
	// distintas partes de Spring usan uno u otro.
	private HttpServletRequest conUsuario(HttpServletRequest original, String idEmpleado, String rol) {
		
		return new HttpServletRequestWrapper(original) {
			
			@Override
			public String getHeader(String nombre) {
				
				if (CABECERA_ID.equalsIgnoreCase(nombre)) {
					return idEmpleado;
				}
				
				if (CABECERA_ROL.equalsIgnoreCase(nombre)) {
					return rol;
				}
				
				return super.getHeader(nombre);
			}
			
			@Override
			public Enumeration<String> getHeaders(String nombre) {
				
				if (CABECERA_ID.equalsIgnoreCase(nombre)) {
					return Collections.enumeration(List.of(idEmpleado));
				}
				
				if (CABECERA_ROL.equalsIgnoreCase(nombre)) {
					return Collections.enumeration(List.of(rol));
				}
				
				return super.getHeaders(nombre);
			}
			
			@Override
			public Enumeration<String> getHeaderNames() {
				
				List<String> nombres = new ArrayList<>(Collections.list(super.getHeaderNames()));
				nombres.add(CABECERA_ID);
				nombres.add(CABECERA_ROL);
				
				return Collections.enumeration(nombres);
			}
		};
	}
	
	// todas las listas se preguntan igual: si la ruta empieza con alguna
	private boolean empieza(List<String> rutas, String ruta) {
		return rutas.stream().anyMatch(ruta::startsWith);
	}
	
	private boolean esLibre(String ruta) {
		return this.empieza(LIBRES, ruta);
	}
	
	private boolean esSoloAdmin(String ruta) {
		
		if (this.empieza(EXCEPCIONES_SOLO_ADMIN, ruta)) {
			return false;
		}
		
		return this.empieza(SOLO_ADMIN, ruta);
	}
	
	private boolean esAdminParaEscribir(String ruta) {
		return this.empieza(ADMIN_PARA_ESCRIBIR, ruta);
	}
	
	// el token viaja como: Authorization: Bearer xxxxx
	private String leerToken(HttpServletRequest peticion) {
		
		String cabecera = peticion.getHeader("Authorization");
		
		if (cabecera == null || !cabecera.startsWith("Bearer ")) {
			return null;
		}
		
		return cabecera.substring(7);
	}
	
	private void cortar(HttpServletResponse respuesta, HttpStatus estado, String mensaje) throws IOException {
		
		respuesta.setStatus(estado.value());
		respuesta.setContentType("text/plain;charset=UTF-8");
		respuesta.getWriter().write(mensaje);
	}
	
	private SecretKey clave() {
		return Keys.hmacShaKeyFor(secreto.getBytes());
	}

}
