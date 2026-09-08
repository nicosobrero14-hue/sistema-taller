package com.tallerapp.taller_service.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PresupuestoDTO {
	
	private Long id;

    @NotNull
    private Long id_vehiculo;

    private Long id_empleado;
    private Long id_sucursal;
    private String detalleTrabajo;

    // por defecto 15 dias
    private Integer validezDias;

    private BigDecimal horasEstimadas;
    private BigDecimal precioHora;

    private List<ItemPresupuestoDTO> items;

}
