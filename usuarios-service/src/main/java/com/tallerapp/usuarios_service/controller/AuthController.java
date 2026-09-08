package com.tallerapp.usuarios_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tallerapp.usuarios_service.dto.LoginDTO;
import com.tallerapp.usuarios_service.dto.SesionDTO;
import com.tallerapp.usuarios_service.service.IAuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	private IAuthService authServ;
	
	//1- entrar al sistema. Es la unica ruta que el gateway deja pasar sin token.
	@PostMapping("/login")
	public SesionDTO login (@Valid @RequestBody LoginDTO loginDTO) {
		
		SesionDTO sesion = authServ.login(loginDTO.getEmail(), loginDTO.getPassword());
		
		if (sesion == null) {
			throw new CredencialesInvalidasException();
		}
		
		return sesion;
	}
	
	//2- cambiar la contraseña de un empleado
	@PutMapping("/password/{id_empleado}")
	public String cambiarPassword (@PathVariable Long id_empleado,
								   @RequestParam String password) {
		
		return authServ.cambiarPassword(id_empleado, password);
	}
	
	
	// 401: el mail o la contraseña no coinciden
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Email o contraseña incorrectos")
	public static class CredencialesInvalidasException extends RuntimeException {
	}

}
