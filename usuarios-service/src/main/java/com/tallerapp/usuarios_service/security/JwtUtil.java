package com.tallerapp.usuarios_service.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

// Arma el token que despues valida el gateway.
// Adentro viajan el email, el nombre y el rol del empleado.
@Component
public class JwtUtil {
	
	@Value("${jwt.secreto}")
	private String secreto;
	
	@Value("${jwt.horas-validez}")
	private long horasValidez;
	
	
	public String generarToken(Long id_empleado, String email, String nombre, String rol) {
		
		long ahora = System.currentTimeMillis();
		long vence = ahora + (horasValidez * 60 * 60 * 1000);
		
		return Jwts.builder()
				.subject(email)
				.claim("id_empleado", id_empleado)
				.claim("nombre", nombre)
				.claim("rol", rol)
				.issuedAt(new Date(ahora))
				.expiration(new Date(vence))
				.signWith(this.clave())
				.compact();
	}
	
	private SecretKey clave() {
		return Keys.hmacShaKeyFor(secreto.getBytes());
	}

}
