package com.tallerapp.taller_service.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ItemPresupuestoDTO {
	
	// si viene, es un repuesto del deposito; si no, es un trabajo suelto
	private Long id_repuesto;
    // en blanco va en todas las opciones; con numero, solo en esa
    private Integer opcion;
	private String descripcion;
	private Integer cantidad;
	private BigDecimal precioUnitario;

}
