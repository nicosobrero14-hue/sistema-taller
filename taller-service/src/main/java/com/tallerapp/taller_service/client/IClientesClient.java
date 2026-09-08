package com.tallerapp.taller_service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import com.tallerapp.taller_service.dto.VehiculoDTO;

// llamadas a clientes-service (puerto 8082)
public interface IClientesClient {
	
	@GetExchange("/vehiculos/traer/{id}")
	VehiculoDTO traerVehiculo(@PathVariable Long id);

}
