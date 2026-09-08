package com.tallerapp.taller_service.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import com.tallerapp.taller_service.dto.EmpleadoDTO;
import com.tallerapp.taller_service.dto.SucursalDTO;

// llamadas a usuarios-service (puerto 8081)
public interface IUsuariosClient {
	
	@GetExchange("/empleados/traer/{id}")
	EmpleadoDTO traerEmpleado(@PathVariable Long id);
	
	@GetExchange("/sucursales/traer/{id}")
	SucursalDTO traerSucursal(@PathVariable Long id);

}
