package com.tallerapp.taller_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// copia minima del empleado que vive en usuarios-service:
// el rol viaja como String para no tener que copiar tambien el enum
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class EmpleadoDTO {
	
	private Long id_empleado;
	private String nombre;
	private String apellido;
	private String rol;

}
