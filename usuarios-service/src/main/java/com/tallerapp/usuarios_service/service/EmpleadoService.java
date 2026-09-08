package com.tallerapp.usuarios_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tallerapp.usuarios_service.client.ITallerClient;
import com.tallerapp.usuarios_service.entity.Empleado;
import com.tallerapp.usuarios_service.entity.Sucursal;
import com.tallerapp.usuarios_service.entity.Rol;
import com.tallerapp.usuarios_service.exception.RecursoNoEncontradoException;
import com.tallerapp.usuarios_service.exception.ReglaNegocioException;
import com.tallerapp.usuarios_service.repository.IEmpleadoRepository;
import com.tallerapp.usuarios_service.repository.ISucursalRepository;

@Service
public class EmpleadoService implements IEmpleadoService {
	
	@Autowired
	private IEmpleadoRepository empleadoRepository;
	
	@Autowired
	private ISucursalRepository sucursalRepository;
	
	@Autowired
	private ITallerClient tallerClient;
	
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Override
	public String saveEmpleado(String nombre, String apellido, String email, String password, Rol rol, Long id_sucursal) {
		
		Sucursal sucursal = sucursalRepository.findById(id_sucursal)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la sucursal con id " + id_sucursal));
		
		// el email es con lo que se entra al sistema, no puede repetirse
		if (empleadoRepository.findByEmail(email).isPresent()) {
			throw new ReglaNegocioException("Ya hay un empleado con el email " + email);
		}
		
		if (password == null || password.length() < 6) {
			throw new ReglaNegocioException("La contraseña tiene que tener al menos 6 caracteres");
		}
		
		Empleado empleado = new Empleado();
		empleado.setNombre(nombre);
		empleado.setApellido(apellido);
		empleado.setEmail(email);
		empleado.setPassword(encoder.encode(password));
		empleado.setRol(rol);
		empleado.setSucursal(sucursal);
		empleado.setActivo(true);
		
		empleadoRepository.save(empleado);
		
		return "Empleado creado correctamente";
	}

	@Override
	public List<Empleado> getEmpleados() {
		
		return empleadoRepository.findAll();
	}

	@Override
	public String deleteEmpleado(Long id) {
		
		this.findEmpleado(id);
		
		// las ordenes guardan el id_mecanico suelto, sin foreign key,
		// asi que le preguntamos a taller-service antes de borrar
		Long ordenes = this.contarOrdenes(id);
		
		if (ordenes == null) {
			throw new ReglaNegocioException("No se puede borrar: taller-service no responde y no se pueden verificar las ordenes");
		}
		
		if (ordenes > 0) {
			throw new ReglaNegocioException("No se puede borrar: el empleado tiene " + ordenes + " orden(es) de trabajo asignada(s)");
		}
		
		empleadoRepository.deleteById(id);
		
		return "El Empleado fue eliminado correctamente";
	}
	
	// devuelve null si taller-service no esta levantado
	private Long contarOrdenes(Long id_mecanico) {
		
		try {
			return tallerClient.contarOrdenes(id_mecanico);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void editEmpleado(Long id, Empleado empleado) {
		
		Empleado emp = this.findEmpleado(id);
		emp.setNombre(empleado.getNombre());
		emp.setApellido(empleado.getApellido());
		emp.setEmail(empleado.getEmail());
		// la contraseña no se cambia aca, va por /auth/password
		emp.setRol(empleado.getRol());
		
		// la sucursal llega solo con el id, hay que buscarla en la base
		if (empleado.getSucursal() != null) {
			
			Long id_sucursal = empleado.getSucursal().getId_sucursal();
			
			emp.setSucursal(sucursalRepository.findById(id_sucursal)
					.orElseThrow(() -> new RecursoNoEncontradoException("No existe la sucursal con id " + id_sucursal)));
		}
		
		empleadoRepository.save(emp);
		
	}

	@Override
	public Empleado findEmpleado(Long id) {
		
		return empleadoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el empleado con id " + id));
	}

	

	/**
	 * Bloquea o desbloquea a un empleado.
	 *
	 * No se puede bloquear a si mismo: si el unico administrador se
	 * bloquea, nadie puede volver a entrar a desbloquearlo.
	 */
	@Override
	public String bloquearEmpleado(Long id, boolean activo, Long id_quienPide) {
		
		Empleado empleado = this.findEmpleado(id);
		
		if (id.equals(id_quienPide)) {
			throw new ReglaNegocioException("No podes bloquearte a vos mismo");
		}
		
		empleado.setActivo(activo);
		empleadoRepository.save(empleado);
		
		return activo
				? empleado.getNombre() + " puede volver a entrar al sistema"
				: empleado.getNombre() + " quedo bloqueado. No va a poder entrar";
	}

}
