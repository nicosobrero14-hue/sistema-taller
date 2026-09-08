package com.tallerapp.taller_service.dto;

import java.math.BigDecimal;

import com.tallerapp.taller_service.entity.TipoItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ItemOrdenTrabajoDTO {
	
	private Long id;

    @NotNull
    private Long id_orden;

    @NotNull
    private TipoItem tipo;

    // se completa en la Fase 2, cuando exista inventario-service
    private Long id_repuesto;

    @NotBlank
    private String descripcion;

    @Min(1)
    private Integer cantidad;

    @NotNull
    private BigDecimal precioUnitario;

}
