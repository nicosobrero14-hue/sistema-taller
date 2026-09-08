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

import com.tallerapp.clientes_service.dto.ClienteDTO;
import com.tallerapp.clientes_service.entity.Cliente;

import jakarta.validation.Valid;
import com.tallerapp.clientes_service.service.IClienteService;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
	
	@Autowired
	private IClienteService clienteServ;
	
	//1- crear un nuevo cliente
	@PostMapping("/crear")
	public String crearCliente (@Valid @RequestBody ClienteDTO clienteDTO) {
		
		clienteServ.saveCliente(clienteDTO.getNombre(),
								clienteDTO.getTelefono(),
								clienteDTO.getEmail());
		
		return "Cliente creado correctamente";
	}
	
	//2- obtener todos los clientes
	@GetMapping("/traer")
	public List<Cliente> traerClientes () {
		return clienteServ.getClientes();
	}
	
	//3- Eliminar un cliente
	@DeleteMapping("/borrar/{id}")
	public String deleteCliente (@PathVariable Long id) {
		return clienteServ.deleteCliente(id);
	}
	
	//4- Editar Cliente
	@PutMapping("/editar/{id_original}")
	public Cliente editCliente (@PathVariable Long id_original,
								@RequestBody Cliente clienteEditar) {
		
		clienteServ.editCliente(id_original, clienteEditar);
		Cliente clienteEditado = clienteServ.findCliente(id_original);
		
		return clienteEditado;
	}
	
	//5- obtener un cliente en particular
	@GetMapping("/traer/{id}")
	public Cliente traerCliente (@PathVariable Long id) {
		return clienteServ.findCliente(id);
	}

}
