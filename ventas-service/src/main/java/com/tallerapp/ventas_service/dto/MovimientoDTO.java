package com.tallerapp.ventas_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// lo que se le manda a inventario-service para mover el stock
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MovimientoDTO {

	private Long id_repuesto;
	private String tipo;
	private Integer cantidad;
	private String motivo;
	private Long id_venta;

}
