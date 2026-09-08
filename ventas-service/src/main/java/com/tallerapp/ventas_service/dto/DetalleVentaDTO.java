package com.tallerapp.ventas_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DetalleVentaDTO {

	@NotNull
	private Long id_repuesto;

	@Min(1)
	private Integer cantidad;

}
