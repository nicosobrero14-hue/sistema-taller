package com.tallerapp.taller_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tallerapp.taller_service.dto.PresupuestoDTO;
import com.tallerapp.taller_service.entity.Presupuesto;

import jakarta.validation.Valid;

import com.tallerapp.taller_service.service.IPresupuestoService;

@RestController
@RequestMapping("/presupuestos")
public class PresupuestoController {
	
	@Autowired
	private IPresupuestoService presupuestoServ;
	
	//1- crear un presupuesto. Los precios quedan congelados por los dias
	// de validez que se indiquen (15 por defecto).
	@PostMapping("/crear")
	public String crearPresupuesto (@Valid @RequestBody PresupuestoDTO presupuestoDTO,
									@RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {
		
		return presupuestoServ.savePresupuesto(presupuestoDTO.getId_vehiculo(),
											   id_empleado,
											   presupuestoDTO.getId_sucursal(),
											   presupuestoDTO.getDetalleTrabajo(),
											   presupuestoDTO.getValidezDias(),
											   presupuestoDTO.getHorasEstimadas(),
											   presupuestoDTO.getPrecioHora(),
											   presupuestoDTO.getItems());
	}
	
	//2- todos los presupuestos
	@GetMapping("/traer")
	public List<Presupuesto> traerPresupuestos () {
		return presupuestoServ.getPresupuestos();
	}
	
	//3- uno en particular
	@GetMapping("/traer/{id}")
	public Presupuesto traerPresupuesto (@PathVariable Long id) {
		return presupuestoServ.findPresupuesto(id);
	}
	
	//4- los de un vehiculo
	@GetMapping("/vehiculo/{id_vehiculo}")
	public List<Presupuesto> traerPorVehiculo (@PathVariable Long id_vehiculo) {
		return presupuestoServ.getPresupuestosPorVehiculo(id_vehiculo);
	}
	
	//5- el cliente eligio una de las alternativas
	@PutMapping("/elegir/{id_presupuesto}")
	public String elegirOpcion (@PathVariable Long id_presupuesto,
								@RequestParam Integer opcion) {

		return presupuestoServ.elegirOpcion(id_presupuesto, opcion);
	}

	//6- el cliente lo acepto: se genera la orden y recien ahi sale el stock
	@PutMapping("/aceptar/{id}")
	public String aceptarPresupuesto (@PathVariable Long id,
									  @RequestParam(required = false) Long id_mecanico,
									  @RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {

		return presupuestoServ.aceptarPresupuesto(id, id_mecanico, id_empleado);
	}
	
	//7- el cliente no lo tomo
	@PutMapping("/rechazar/{id}")
	public String rechazarPresupuesto (@PathVariable Long id) {
		return presupuestoServ.rechazarPresupuesto(id);
	}
	
	//8- vencido: se estira la validez desde hoy
	@PutMapping("/renovar/{id}")
	public String renovarPresupuesto (@PathVariable Long id) {
		return presupuestoServ.renovarPresupuesto(id);
	}
	
	//9- borrar
	@DeleteMapping("/borrar/{id}")
	public String deletePresupuesto (@PathVariable Long id) {
		return presupuestoServ.deletePresupuesto(id);
	}

}
