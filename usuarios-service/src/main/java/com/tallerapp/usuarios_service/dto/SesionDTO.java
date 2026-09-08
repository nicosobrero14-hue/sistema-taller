package com.tallerapp.usuarios_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// lo que recibe el front cuando entra: el token y con quien esta trabajando
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SesionDTO {
	
	private String token;
	private Long id_empleado;
	private String nombre;
	private String email;
	private String rol;

}
