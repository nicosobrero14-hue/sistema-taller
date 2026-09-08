package com.tallerapp.taller_service.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

import com.tallerapp.taller_service.dto.MovimientoDTO;

// llamadas a inventario-service (puerto 8084)
public interface IInventarioClient {
	
	// registra la salida o la devolucion del repuesto. Si no hay stock,
	// inventario-service responde 409 y el RestClient lo convierte en excepcion.
	@PostExchange("/movimientos/crear")
	String moverStock(@RequestBody MovimientoDTO movimiento);

}
