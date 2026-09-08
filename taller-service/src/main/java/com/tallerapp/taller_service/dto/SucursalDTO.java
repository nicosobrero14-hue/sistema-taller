package com.tallerapp.taller_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// copia minima de la sucursal que vive en usuarios-service
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SucursalDTO {
	
	private Long id_sucursal;
	private String nombre;

}
