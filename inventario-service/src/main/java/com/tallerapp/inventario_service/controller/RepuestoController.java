package com.tallerapp.inventario_service.controller;

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

import com.tallerapp.inventario_service.dto.RepuestoDTO;
import com.tallerapp.inventario_service.entity.PrecioRepuesto;
import com.tallerapp.inventario_service.entity.Repuesto;

import jakarta.validation.Valid;

import com.tallerapp.inventario_service.service.IRepuestoService;

@RestController
@RequestMapping("/repuestos")
public class RepuestoController {
	
	@Autowired
	private IRepuestoService repuestoServ;
	
	//1- crear un nuevo repuesto (carga a mano)
	@PostMapping("/crear")
	public String crearRepuesto (@Valid @RequestBody RepuestoDTO repuestoDTO) {
		
		return repuestoServ.saveRepuesto(repuestoDTO.getCodigo(),
										 repuestoDTO.getCodigoBarra(),
										 repuestoDTO.getNombre(),
										 repuestoDTO.getDescripcion(),
										 repuestoDTO.getMarca(),
										 repuestoDTO.getUbicacion(),
										 repuestoDTO.getPrecioCompra(),
										 repuestoDTO.getPrecioVenta(),
										 repuestoDTO.getStock(),
										 repuestoDTO.getStockMinimo(),
										 repuestoDTO.getId_sucursal());
	}
	
	//2- obtener todos los repuestos
	@GetMapping("/traer")
	public List<Repuesto> traerRepuestos () {
		return repuestoServ.getRepuestos();
	}
	
	//3- Eliminar un repuesto
	@DeleteMapping("/borrar/{id}")
	public String deleteRepuesto (@PathVariable Long id) {
		return repuestoServ.deleteRepuesto(id);
	}
	
	//4- Editar Repuesto. El stock no se cambia aca, se cambia con un movimiento.
	@PutMapping("/editar/{id_original}")
	public String editRepuesto (@PathVariable Long id_original,
								@RequestBody Repuesto repuestoEditar,
								@RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {
		
		return repuestoServ.editRepuesto(id_original, repuestoEditar, id_empleado);
	}
	
	//5- obtener un repuesto en particular
	@GetMapping("/traer/{id}")
	public Repuesto traerRepuesto (@PathVariable Long id) {
		return repuestoServ.findRepuesto(id);
	}
	
	//6- lo que dispara la pistola de codigo de barras: codigo exacto,
	// devuelve un solo repuesto o 404
	@GetMapping("/codigo/{codigoBarra}")
	public Repuesto traerPorCodigoBarra (@PathVariable String codigoBarra) {
		return repuestoServ.findPorCodigoBarra(codigoBarra);
	}
	
	//7- buscador del deposito: texto parcial contra codigo, codigo de barras,
	// nombre y marca
	@GetMapping("/buscar")
	public List<Repuesto> buscarRepuestos (@RequestParam(required = false) String texto) {
		return repuestoServ.buscar(texto);
	}
	
	//8- como fue cambiando el precio de este repuesto
	@GetMapping("/precios/{id_repuesto}")
	public List<PrecioRepuesto> traerHistorialPrecios (@PathVariable Long id_repuesto) {
		return repuestoServ.getHistorialPrecios(id_repuesto);
	}
	
	//9- los que llegaron al stock minimo y hay que reponer
	@GetMapping("/bajo-stock")
	public List<Repuesto> traerBajoStock () {
		return repuestoServ.getBajoStock();
	}

}
