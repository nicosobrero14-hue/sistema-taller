package com.tallerapp.usuarios_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tallerapp.usuarios_service.dto.SesionDTO;
import com.tallerapp.usuarios_service.entity.Empleado;
import com.tallerapp.usuarios_service.exception.RecursoNoEncontradoException;
import com.tallerapp.usuarios_service.exception.ReglaNegocioException;
import com.tallerapp.usuarios_service.repository.IEmpleadoRepository;
import com.tallerapp.usuarios_service.security.JwtUtil;

@Service
public class AuthService implements IAuthService {
	
	@Autowired
	private IEmpleadoRepository empleadoRepository;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	// BCrypt: la contraseña se guarda hasheada y nunca se puede volver atras.
	// Para verificar se hashea lo que escribio el usuario y se comparan.
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	
	
	@Override
	public SesionDTO login(String email, String password) {
		
		Empleado empleado = empleadoRepository.findByEmail(email).orElse(null);
		
		// El mismo mensaje si el mail no existe o si la clave esta mal:
		// asi no se puede averiguar que mails estan dados de alta.
		if (empleado == null || empleado.getPassword() == null
				|| !encoder.matches(password, empleado.getPassword())) {
			return null;
		}
		
		// Bloqueado no entra. Aca si se dice el motivo: el mail y la clave
		// ya se validaron, asi que no se le esta contando nada a un
		// desconocido.
		if (Boolean.FALSE.equals(empleado.getActivo())) {
			throw new EmpleadoBloqueadoException();
		}
		
		String token = jwtUtil.generarToken(empleado.getId_empleado(),
											empleado.getEmail(),
											empleado.getNombre(),
											empleado.getRol().name());
		
		SesionDTO sesion = new SesionDTO();
		sesion.setToken(token);
		sesion.setId_empleado(empleado.getId_empleado());
		sesion.setNombre(empleado.getNombre() + " " + (empleado.getApellido() == null ? "" : empleado.getApellido()));
		sesion.setEmail(empleado.getEmail());
		sesion.setRol(empleado.getRol().name());
		
		return sesion;
	}

	@Override
	public String cambiarPassword(Long id_empleado, String passwordNueva) {
		
		Empleado empleado = empleadoRepository.findById(id_empleado)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el empleado con id " + id_empleado));
		
		if (passwordNueva == null || passwordNueva.length() < 6) {
			throw new ReglaNegocioException("La contraseña tiene que tener al menos 6 caracteres");
		}
		
		empleado.setPassword(encoder.encode(passwordNueva));
		empleadoRepository.save(empleado);
		
		return "Contraseña actualizada correctamente";
	}

	// 403: el usuario existe y la clave esta bien, pero esta bloqueado
	@org.springframework.web.bind.annotation.ResponseStatus(
			value = org.springframework.http.HttpStatus.FORBIDDEN,
			reason = "Tu usuario esta bloqueado. Hablá con el administrador")
	public static class EmpleadoBloqueadoException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

}
