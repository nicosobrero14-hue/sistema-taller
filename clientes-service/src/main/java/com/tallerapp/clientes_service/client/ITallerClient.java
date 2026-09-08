package com.tallerapp.clientes_service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

// llamadas a taller-service (puerto 8083)
public interface ITallerClient {
	
	// cuantas ordenes de trabajo tiene el vehiculo
	@GetExchange("/ordenes/contar/vehiculo/{id}")
	Long contarOrdenes(@PathVariable Long id);

}
