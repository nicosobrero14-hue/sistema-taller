package com.tallerapp.taller_service.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

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

import com.tallerapp.taller_service.dto.OrdenTrabajoDTO;
import com.tallerapp.taller_service.entity.EstadoOrden;
import com.tallerapp.taller_service.entity.MovimientoOrden;
import com.tallerapp.taller_service.entity.OrdenTrabajo;

import jakarta.validation.Valid;
import com.tallerapp.taller_service.service.IOrdenTrabajoService;

@RestController
@RequestMapping("/ordenes")
public class OrdenTrabajoController {
	
	@Autowired
	private IOrdenTrabajoService ordenServ;
	
	//1- crear una nueva orden de trabajo
	@PostMapping("/crear")
	public String crearOrden (@Valid @RequestBody OrdenTrabajoDTO ordenDTO,
							  @RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {
		
		return ordenServ.saveOrden(ordenDTO.getId_vehiculo(),
								   ordenDTO.getId_mecanico(),
								   ordenDTO.getId_sucursal(),
								   ordenDTO.getDiagnostico(),
								   ordenDTO.getFechaEntregaEstimada(),
								   ordenDTO.getKilometrajeIngreso(),
									   id_empleado);
	}
	
	//2- obtener todas las ordenes
	@GetMapping("/traer")
	public List<OrdenTrabajo> traerOrdenes () {
		return ordenServ.getOrdenes();
	}
	
	//3- Eliminar una orden
	@DeleteMapping("/borrar/{id}")
	public String deleteOrden (@PathVariable Long id) {
		return ordenServ.deleteOrden(id);
	}
	
	//4- Editar Orden. Devuelve el mensaje porque, igual que al crear,
	// valida el vehiculo, el mecanico y la sucursal contra los otros servicios.
	@PutMapping("/editar/{id_original}")
	public String editOrden (@PathVariable Long id_original,
							 @RequestBody OrdenTrabajo ordenEditar) {
		
		return ordenServ.editOrden(id_original, ordenEditar);
	}
	
	//5- obtener una orden en particular
	@GetMapping("/traer/{id}")
	public OrdenTrabajo traerOrden (@PathVariable Long id) {
		return ordenServ.findOrden(id);
	}
	
	//2b- las que estan en el taller ahora. Es la lista del dia a dia.
	@GetMapping("/abiertas")
	public List<OrdenTrabajo> traerAbiertas () {
		return ordenServ.getAbiertas();
	}

	//2c- el archivo: las entregadas entre dos fechas
	@GetMapping("/entregadas")
	public List<OrdenTrabajo> traerEntregadas (@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate desde,
											   @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate hasta) {

		return ordenServ.getEntregadas(desde, hasta);
	}

	//6- cambiar solo el estado, sin tener que mandar la orden entera
	@PutMapping("/estado/{id}")
	public String cambiarEstado (@PathVariable Long id,
								 @RequestParam EstadoOrden estado,
								 @RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {
		
		return ordenServ.cambiarEstado(id, estado, id_empleado);
	}
	
	//6c- la usa pagos-service para avisar que la orden quedo cobrada
	@PutMapping("/pagada/{id}")
	public String marcarPagada (@PathVariable Long id, @RequestParam boolean pagada) {

		ordenServ.marcarPagada(id, pagada);

		return "Listo";
	}

	//6b- quien movio esta orden y cuando
	@GetMapping("/movimientos/{id}")
	public List<MovimientoOrden> traerMovimientos (@PathVariable Long id) {
		return ordenServ.getMovimientos(id);
	}
	
	//7- historial: todas las ordenes que tuvo un vehiculo
	@GetMapping("/vehiculo/{id_vehiculo}")
	public List<OrdenTrabajo> traerOrdenesPorVehiculo (@PathVariable Long id_vehiculo) {
		return ordenServ.getOrdenesPorVehiculo(id_vehiculo);
	}
	
	//8- cuantas ordenes tiene un vehiculo. Lo consulta clientes-service
	// antes de dejar borrar el vehiculo.
	@GetMapping("/contar/vehiculo/{id_vehiculo}")
	public Long contarPorVehiculo (@PathVariable Long id_vehiculo) {
		return ordenServ.contarPorVehiculo(id_vehiculo);
	}
	
	//9- lo mismo para el mecanico, lo consulta usuarios-service
	@GetMapping("/contar/mecanico/{id_mecanico}")
	public Long contarPorMecanico (@PathVariable Long id_mecanico) {
		return ordenServ.contarPorMecanico(id_mecanico);
	}

}
