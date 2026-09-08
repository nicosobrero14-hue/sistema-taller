package com.tallerapp.taller_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// lo que taller-service le manda a inventario-service para que mueva el stock.
// El tipo viaja como String para no tener que copiar el enum del otro servicio.
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MovimientoDTO {
	
	private Long id_repuesto;
	private String tipo;
	private Integer cantidad;
	private String motivo;
	private Long id_orden;

}
