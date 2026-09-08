package com.tallerapp.taller_service.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrdenTrabajoDTO {
	
	private Long id;

    @NotNull
    private Long id_vehiculo;

    @NotNull
    private Long id_mecanico;

    @NotNull
    private Long id_sucursal;

    private String diagnostico;

    // con cuantos km entro el vehiculo
    private Integer kilometrajeIngreso;

    private LocalDateTime fechaEntregaEstimada;

    // descuento en pesos sobre el total de la orden
    private java.math.BigDecimal descuento;

}
