package com.tallerapp.ventas_service.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// lo que ventas-service necesita saber de un repuesto de inventario-service
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RepuestoDTO {

	private Long id_repuesto;
	private String codigo;
	private String nombre;
	private String marca;
	private Integer stock;
	private BigDecimal precioVenta;

}
