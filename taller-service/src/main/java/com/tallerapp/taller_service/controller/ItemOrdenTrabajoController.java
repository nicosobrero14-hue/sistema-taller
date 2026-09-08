package com.tallerapp.taller_service.controller;

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

import com.tallerapp.taller_service.dto.ItemOrdenTrabajoDTO;
import com.tallerapp.taller_service.entity.ItemOrdenTrabajo;

import jakarta.validation.Valid;
import com.tallerapp.taller_service.service.IItemOrdenTrabajoService;

@RestController
@RequestMapping("/items")
public class ItemOrdenTrabajoController {
	
	@Autowired
	private IItemOrdenTrabajoService itemServ;
	
	//1- crear un nuevo item
	@PostMapping("/crear")
	public String crearItem (@Valid @RequestBody ItemOrdenTrabajoDTO itemDTO) {
		
		return itemServ.saveItem(itemDTO.getId_orden(),
								 itemDTO.getTipo(),
								 itemDTO.getId_repuesto(),
								 itemDTO.getDescripcion(),
								 itemDTO.getCantidad(),
								 itemDTO.getPrecioUnitario());
	}
	
	//2- obtener todos los items
	@GetMapping("/traer")
	public List<ItemOrdenTrabajo> traerItems () {
		return itemServ.getItems();
	}
	
	//3- Eliminar un item
	@DeleteMapping("/borrar/{id}")
	public String deleteItem (@PathVariable Long id) {
		return itemServ.deleteItem(id);
	}
	
	//4- Editar Item
	@PutMapping("/editar/{id_original}")
	public String editItem (@PathVariable Long id_original,
							@RequestBody ItemOrdenTrabajo itemEditar) {
		
		return itemServ.editItem(id_original, itemEditar);
	}
	
	//5- obtener un item en particular
	@GetMapping("/traer/{id}")
	public ItemOrdenTrabajo traerItem (@PathVariable Long id) {
		return itemServ.findItem(id);
	}

}
