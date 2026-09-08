package com.tallerapp.inventario_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RepuestoDTO {
	
	private Long id;

    @NotBlank
    private String codigo;

    // opcional: no todos los repuestos vienen con codigo de barras
    private String codigoBarra;

    @NotBlank
    private String nombre;

    private String descripcion;

    private String marca;

    private String ubicacion;

    private BigDecimal precioCompra;

    private BigDecimal precioVenta;

    @Min(0)
    private Integer stock;

    @Min(0)
    private Integer stockMinimo;

    private Long id_sucursal;

}
