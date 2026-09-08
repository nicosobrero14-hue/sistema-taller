package com.tallerapp.usuarios_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerapp.usuarios_service.dto.DatosCobroDTO;
import com.tallerapp.usuarios_service.entity.ConfiguracionTaller;
import com.tallerapp.usuarios_service.service.IConfiguracionService;

// Los datos del taller: razon social, CUIT y el precio de la hora.
// El gateway solo deja entrar aca a los ADMIN.
@RestController
@RequestMapping("/configuracion")
public class ConfiguracionController {
	
	@Autowired
	private IConfiguracionService configuracionServ;
	
	//1- traer los datos del taller
	@GetMapping("/traer")
	public ConfiguracionTaller traerConfiguracion () {
		return configuracionServ.getConfiguracion();
	}
	
	//2- guardarlos
	@PutMapping("/editar")
	public String editConfiguracion (@RequestBody ConfiguracionTaller configuracion) {
		return configuracionServ.editConfiguracion(configuracion);
	}
	
	//3- El encabezado del comprobante y adonde paga el cliente. Es la unica
	// ruta de /configuracion que puede leer cualquier empleado: el que
	// atiende necesita dictar el CBU e imprimir el recibo con membrete.
	@GetMapping("/cobro")
	public DatosCobroDTO traerDatosCobro () {
		return configuracionServ.getDatosCobro();
	}

}
