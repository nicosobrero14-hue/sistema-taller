package com.tallerapp.pagos_service.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// copia minima de la orden que vive en taller-service:
// lo unico que necesita pagos es cuanto hay que cobrar y como viene
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrdenDTO {
	
	private Long id_orden;
	private Long id_vehiculo;
	private String estado;
	private String diagnostico;
	private BigDecimal total;

}
