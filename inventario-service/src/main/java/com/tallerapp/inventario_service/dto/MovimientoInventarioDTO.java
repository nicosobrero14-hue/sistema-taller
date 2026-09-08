package com.tallerapp.inventario_service.dto;

import com.tallerapp.inventario_service.entity.TipoMovimiento;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MovimientoInventarioDTO {
	
	private Long id;

    @NotNull
    private Long id_repuesto;

    @NotNull
    private TipoMovimiento tipo;

    @Min(0)
    private Integer cantidad;

    private String motivo;

    // si la salida es por una orden de trabajo o por una venta de mostrador
    private Long id_orden;
    private Long id_venta;

}
