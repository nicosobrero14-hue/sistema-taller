package com.tallerapp.pagos_service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PutExchange;

import com.tallerapp.pagos_service.dto.OrdenDTO;

// llamadas a taller-service (puerto 8083)
public interface ITallerClient {

	@GetExchange("/ordenes/traer/{id}")
	OrdenDTO traerOrden(@PathVariable Long id);

	// le avisa que la orden quedo cobrada, o que dejo de estarlo
	@PutExchange("/ordenes/pagada/{id_orden}")
	String marcarPagada(@PathVariable Long id_orden, @RequestParam boolean pagada);

}
