package com.tallerapp.clientes_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class VehiculoDTO {
	
	private Long id;

    @NotBlank
    private String patente;

    private String marca;

    private String modelo;

    private Integer anio;

    private Integer kilometraje;

    @NotNull
    private Long id_cliente;

}
