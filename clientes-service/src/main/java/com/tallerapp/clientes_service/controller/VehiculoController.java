package com.tallerapp.clientes_service.controller;

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

import com.tallerapp.clientes_service.dto.VehiculoDTO;
import com.tallerapp.clientes_service.entity.Vehiculo;

import jakarta.validation.Valid;
import com.tallerapp.clientes_service.service.IVehiculoService;

@RestController
@RequestMapping("/vehiculos")
public class VehiculoController {
	
	@Autowired
	private IVehiculoService vehiculoServ;
	
	//1- crear un nuevo vehiculo
	@PostMapping("/crear")
	public String crearVehiculo (@Valid @RequestBody VehiculoDTO vehiculoDTO) {
		
		vehiculoServ.saveVehiculo(vehiculoDTO.getPatente(),
								  vehiculoDTO.getMarca(),
								  vehiculoDTO.getModelo(),
								  vehiculoDTO.getAnio(),
								  vehiculoDTO.getKilometraje(),
								  vehiculoDTO.getId_cliente());
		
		return "Vehiculo creado correctamente";
	}
	
	//2- obtener todos los vehiculos
	@GetMapping("/traer")
	public List<Vehiculo> traerVehiculos () {
		return vehiculoServ.getVehiculos();
	}
	
	//3- Eliminar un vehiculo
	@DeleteMapping("/borrar/{id}")
	public String deleteVehiculo (@PathVariable Long id) {
		return vehiculoServ.deleteVehiculo(id);
	}
	
	//4- Editar Vehiculo
	@PutMapping("/editar/{id_original}")
	public Vehiculo editVehiculo (@PathVariable Long id_original,
								  @RequestBody Vehiculo vehiculoEditar) {
		
		vehiculoServ.editVehiculo(id_original, vehiculoEditar);
		Vehiculo vehiculoEditado = vehiculoServ.findVehiculo(id_original);
		
		return vehiculoEditado;
	}
	
	//5- obtener un vehiculo en particular
	@GetMapping("/traer/{id}")
	public Vehiculo traerVehiculo (@PathVariable Long id) {
		return vehiculoServ.findVehiculo(id);
	}

}
