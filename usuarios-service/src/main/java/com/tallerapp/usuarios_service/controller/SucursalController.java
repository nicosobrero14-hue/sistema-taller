package com.tallerapp.usuarios_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerapp.usuarios_service.dto.SucursalDTO;
import com.tallerapp.usuarios_service.entity.Sucursal;

import jakarta.validation.Valid;
import com.tallerapp.usuarios_service.service.ISucursalService;

@RestController
@RequestMapping("/sucursales")
public class SucursalController {
	
	@Autowired
	private ISucursalService sucursalServ;
	
	//1- crear una nueva sucursal
	@PostMapping("/crear")
	public String crearSucursal (@Valid @RequestBody SucursalDTO sucursalDTO) {
		
		sucursalServ.saveSucursal(sucursalDTO.getNombre(), sucursalDTO.getDireccion());
		
		return "Sucursal creada correctamente";
	}
	
	//2- obtener todas las sucursales
	@GetMapping("/traer")
	public List<Sucursal> traerSucursales () {
		return sucursalServ.getSucursal();
	}
	
	//3- Eliminar una sucursal
	@DeleteMapping("/borrar/{id}")
	public String deleteSucursal (@PathVariable Long id) {
		return sucursalServ.deleteSucursal(id);
	}
	
	//4- Editar Sucursal
	@PutMapping("/editar/{id_original}")
	public Sucursal editSucursal (@PathVariable Long id_original,
								  @RequestBody Sucursal sucursalEditar) {
		
		sucursalServ.editSucursal(id_original, sucursalEditar);
		Sucursal sucursalEditada = sucursalServ.findSucursal(id_original);
		
		return sucursalEditada;
	}
	
	//5- obtener una sucursal en particular
	@GetMapping("/traer/{id}")
	public Sucursal traerSucursal (@PathVariable Long id) {
		return sucursalServ.findSucursal(id);
	}

}
