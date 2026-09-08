package com.tallerapp.taller_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// copia minima del vehiculo que vive en clientes-service:
// solo los campos que taller-service necesita mostrar o validar
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class VehiculoDTO {
	
	private Long id_vehiculo;
	private String patente;
	private String marca;
	private String modelo;

}
