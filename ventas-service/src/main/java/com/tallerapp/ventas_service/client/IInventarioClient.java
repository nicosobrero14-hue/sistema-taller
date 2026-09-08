package com.tallerapp.ventas_service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.tallerapp.ventas_service.dto.MovimientoDTO;
import com.tallerapp.ventas_service.dto.RepuestoDTO;

// llamadas a inventario-service (puerto 8084)
public interface IInventarioClient {

	@GetExchange("/repuestos/traer/{id}")
	RepuestoDTO traerRepuesto(@PathVariable Long id);

	// registra la salida o la vuelta del repuesto. Si no hay stock,
	// inventario-service responde 409 y el RestClient lo convierte en
	// excepcion.
	@PostExchange("/movimientos/crear")
	String moverStock(@RequestBody MovimientoDTO movimiento);

}
