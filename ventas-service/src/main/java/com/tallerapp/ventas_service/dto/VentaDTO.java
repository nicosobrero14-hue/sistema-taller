package com.tallerapp.ventas_service.dto;

import java.util.List;

import com.tallerapp.ventas_service.entity.MedioVenta;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Lo unico que manda el navegador es que repuestos y cuantos: los precios
// los pone el servicio preguntandole al deposito.
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class VentaDTO {

	private Long id;

	@NotNull
	private MedioVenta medio;

	@NotNull
	private Long id_sucursal;

	// opcional: si no se elige un cliente, es consumidor final
	private Long id_cliente;
	private String nombreCliente;

	private String observaciones;

	@NotEmpty
	private List<DetalleVentaDTO> detalles;

}
