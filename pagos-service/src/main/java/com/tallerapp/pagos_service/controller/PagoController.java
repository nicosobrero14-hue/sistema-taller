package com.tallerapp.pagos_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tallerapp.pagos_service.dto.PagoDTO;
import com.tallerapp.pagos_service.entity.Pago;

import jakarta.validation.Valid;

import com.tallerapp.pagos_service.service.IPagoService;

@RestController
@RequestMapping("/pagos")
public class PagoController {
	
	@Autowired
	private IPagoService pagoServ;
	
	//1- registrar el cobro de una orden. Queda pendiente hasta que
	// alguien confirme que la plata entro.
	@PostMapping("/crear")
	public String crearPago (@Valid @RequestBody PagoDTO pagoDTO,
							 @RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {
		
		return pagoServ.savePago(pagoDTO.getId_orden(),
								 pagoDTO.getMonto(),
								 pagoDTO.getMedio(),
								 pagoDTO.getObservaciones(),
								 id_empleado);
	}
	
	//2- todos los cobros
	@GetMapping("/traer")
	public List<Pago> traerPagos () {
		return pagoServ.getPagos();
	}
	
	//3- uno en particular
	@GetMapping("/traer/{id}")
	public Pago traerPago (@PathVariable Long id) {
		return pagoServ.findPago(id);
	}
	
	//4- el cobro de una orden. Devuelve vacio si todavia no se cobro.
	@GetMapping("/orden/{id_orden}")
	public Pago traerPagoPorOrden (@PathVariable Long id_orden) {
		return pagoServ.findPagoPorOrden(id_orden);
	}
	
	//4b- todos los cobros de una orden, anulados incluidos
	@GetMapping("/orden/{id_orden}/historial")
	public List<Pago> traerHistorialDeOrden (@PathVariable Long id_orden) {
		return pagoServ.getPagosDeOrden(id_orden);
	}
	
	//5- confirmar que la plata entro. Aca se emite el recibo.
	@PutMapping("/confirmar/{id}")
	public String confirmarPago (@PathVariable Long id,
								 @RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {

		return pagoServ.confirmarPago(id, id_empleado);
	}

	//6- anular un cobro. No se borra: queda la fila con el motivo a la vista.
	@PutMapping("/anular/{id}")
	public String anularPago (@PathVariable Long id,
							  @RequestParam String motivo,
							  @RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {
		
		return pagoServ.anularPago(id, motivo, id_empleado);
	}

}
