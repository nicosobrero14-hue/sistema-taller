package com.tallerapp.inventario_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerapp.inventario_service.dto.MovimientoInventarioDTO;
import com.tallerapp.inventario_service.entity.MovimientoInventario;

import jakarta.validation.Valid;

import com.tallerapp.inventario_service.service.IMovimientoInventarioService;

@RestController
@RequestMapping("/movimientos")
public class MovimientoInventarioController {
	
	@Autowired
	private IMovimientoInventarioService movimientoServ;
	
	//1- registrar una entrada, una salida o un ajuste. Es la unica forma
	// de que cambie el stock de un repuesto.
	@PostMapping("/crear")
	public String crearMovimiento (@Valid @RequestBody MovimientoInventarioDTO movimientoDTO) {
		
		return movimientoServ.saveMovimiento(movimientoDTO.getId_repuesto(),
											 movimientoDTO.getTipo(),
											 movimientoDTO.getCantidad(),
											 movimientoDTO.getMotivo(),
											 movimientoDTO.getId_orden(),
											 movimientoDTO.getId_venta());
	}
	
	//2- obtener todos los movimientos
	@GetMapping("/traer")
	public List<MovimientoInventario> traerMovimientos () {
		return movimientoServ.getMovimientos();
	}
	
	//3- obtener un movimiento en particular
	@GetMapping("/traer/{id}")
	public MovimientoInventario traerMovimiento (@PathVariable Long id) {
		return movimientoServ.findMovimiento(id);
	}
	
	//4- historial del deposito para un repuesto
	@GetMapping("/repuesto/{id_repuesto}")
	public List<MovimientoInventario> traerPorRepuesto (@PathVariable Long id_repuesto) {
		return movimientoServ.getMovimientosPorRepuesto(id_repuesto);
	}

}
