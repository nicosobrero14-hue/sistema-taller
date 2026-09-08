package com.tallerapp.pagos_service.dto;

import java.math.BigDecimal;

import com.tallerapp.pagos_service.entity.MedioPago;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class PagoDTO {
	
	private Long id;

    @NotNull
    private Long id_orden;

    @NotNull
    private BigDecimal monto;

    @NotNull
    private MedioPago medio;

    private String observaciones;
    private Long id_empleado;

}
