package com.tallerapp.ventas_service.controller;

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

import com.tallerapp.ventas_service.dto.VentaDTO;
import com.tallerapp.ventas_service.entity.Venta;
import com.tallerapp.ventas_service.service.IVentaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ventas")
public class VentaController {
	
	@Autowired
	private IVentaService ventaServ;
	
	//1- vender repuestos por mostrador
	@PostMapping("/crear")
	public String crearVenta (@Valid @RequestBody VentaDTO ventaDTO,
							  @RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {
		
		return ventaServ.saveVenta(ventaDTO, id_empleado);
	}
	
	//2- todas las ventas, de la mas nueva a la mas vieja
	@GetMapping("/traer")
	public List<Venta> traerVentas () {
		return ventaServ.getVentas();
	}
	
	//3- una en particular
	@GetMapping("/traer/{id}")
	public Venta traerVenta (@PathVariable Long id) {
		return ventaServ.findVenta(id);
	}
	
	//4- anular una venta. Los repuestos vuelven al deposito.
	@PutMapping("/anular/{id}")
	public String anularVenta (@PathVariable Long id,
							   @RequestParam String motivo,
							   @RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {
		
		return ventaServ.anularVenta(id, motivo, id_empleado);
	}

}
