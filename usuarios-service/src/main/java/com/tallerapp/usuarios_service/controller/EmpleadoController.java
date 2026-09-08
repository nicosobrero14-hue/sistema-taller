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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tallerapp.usuarios_service.dto.EmpleadoDTO;
import com.tallerapp.usuarios_service.entity.Empleado;

import jakarta.validation.Valid;
import com.tallerapp.usuarios_service.service.IEmpleadoService;

@RestController
@RequestMapping("/empleados")
public class EmpleadoController {
	
	@Autowired
	private IEmpleadoService empleadoServ;
	
	//1- crear un nuevo empleado
	@PostMapping("/crear")
	public String crearEmpleado (@Valid @RequestBody EmpleadoDTO empleadoDTO) {
		
		return empleadoServ.saveEmpleado(empleadoDTO.getNombre(),
										 empleadoDTO.getApellido(),
										 empleadoDTO.getEmail(),
										 empleadoDTO.getPassword(),
										 empleadoDTO.getRol(),
										 empleadoDTO.getId_sucursal());
	}
	
	//2- obtener todos los empleados
	@GetMapping("/traer")
	public List<Empleado> traerEmpleados () {
		return empleadoServ.getEmpleados();
	}
	
	//3- Eliminar un empleado
	@DeleteMapping("/borrar/{id}")
	public String deleteEmpleado (@PathVariable Long id) {
		return empleadoServ.deleteEmpleado(id);
	}
	
	//4- Editar Empleado
	@PutMapping("/editar/{id_original}")
	public Empleado editEmpleado (@PathVariable Long id_original,
								  @RequestBody Empleado empleadoEditar) {
		
		empleadoServ.editEmpleado(id_original, empleadoEditar);
		Empleado empleadoEditado = empleadoServ.findEmpleado(id_original);
		
		return empleadoEditado;
	}
	
	//4b- bloquear o desbloquear a un empleado. Bloqueado no puede entrar,
	// pero sus ordenes y sus cobros siguen existiendo.
	@PutMapping("/bloquear/{id}")
	public String bloquearEmpleado (@PathVariable Long id,
									@RequestParam boolean activo,
									@RequestHeader(value = "X-Empleado-Id", required = false) Long id_quienPide) {
		
		return empleadoServ.bloquearEmpleado(id, activo, id_quienPide);
	}
	
	//5- obtener un empleado en particular
	@GetMapping("/traer/{id}")
	public Empleado traerEmpleado (@PathVariable Long id) {
		return empleadoServ.findEmpleado(id);
	}

}
