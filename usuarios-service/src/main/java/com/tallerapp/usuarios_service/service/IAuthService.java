package com.tallerapp.usuarios_service.service;

import com.tallerapp.usuarios_service.dto.SesionDTO;

public interface IAuthService {
	
	public SesionDTO login(String email, String password);
	public String cambiarPassword(Long id_empleado, String passwordNueva);

}
