package com.tallerapp.taller_service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import com.tallerapp.taller_service.client.IInventarioClient;
import com.tallerapp.taller_service.dto.MovimientoDTO;
import com.tallerapp.taller_service.entity.ItemOrdenTrabajo;
import com.tallerapp.taller_service.entity.OrdenTrabajo;
import com.tallerapp.taller_service.entity.TipoItem;
import com.tallerapp.taller_service.exception.RecursoNoEncontradoException;
import com.tallerapp.taller_service.exception.ReglaNegocioException;
import com.tallerapp.taller_service.repository.IItemOrdenTrabajoRepository;
import com.tallerapp.taller_service.repository.IOrdenTrabajoRepository;

@Service
public class ItemOrdenTrabajoService implements IItemOrdenTrabajoService {
	
	@Autowired
	private IItemOrdenTrabajoRepository itemRepository;
	
	@Autowired
	private IOrdenTrabajoRepository ordenRepository;
	
	@Autowired
	private IInventarioClient inventarioClient;
	
	
	@Override
	public String saveItem(Long id_orden, TipoItem tipo, Long id_repuesto, String descripcion, Integer cantidad, BigDecimal precioUnitario) {
		
		OrdenTrabajo orden = ordenRepository.findById(id_orden)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden con id " + id_orden));
		
		// Si el item es un repuesto del deposito, primero hay que sacarlo del
		// inventario. Si no hay stock, el item no se carga: no tiene sentido
		// facturar algo que no se puede poner.
		if (tipo == TipoItem.REPUESTO && id_repuesto != null) {
			
			String error = this.moverStock(id_repuesto, "SALIDA", cantidad,
					"Usado en la orden de trabajo " + id_orden, id_orden);
			
			if (error != null) {
				throw new ReglaNegocioException(error);
			}
		}
		
		ItemOrdenTrabajo item = new ItemOrdenTrabajo();
		item.setOrdenTrabajo(orden);
		item.setTipo(tipo);
		item.setId_repuesto(id_repuesto);
		item.setDescripcion(descripcion);
		item.setCantidad(cantidad);
		item.setPrecioUnitario(precioUnitario);
		
		itemRepository.save(item);
		
		return "Item creado correctamente";
	}
	
	// Le pide a inventario-service que mueva el stock.
	// Devuelve null si salio bien, o el mensaje del problema si no.
	private String moverStock(Long id_repuesto, String tipo, Integer cantidad, String motivo, Long id_orden) {
		
		MovimientoDTO movimiento = new MovimientoDTO();
		movimiento.setId_repuesto(id_repuesto);
		movimiento.setTipo(tipo);
		movimiento.setCantidad(cantidad);
		movimiento.setMotivo(motivo);
		movimiento.setId_orden(id_orden);
		
		try {
			inventarioClient.moverStock(movimiento);
			return null;
			
		} catch (RestClientResponseException e) {
			// inventario-service contesto con un error (409 si no hay stock,
			// 404 si el repuesto no existe) y manda el motivo en el cuerpo
			return e.getResponseBodyAsString();
			
		} catch (Exception e) {
			return "No se pudo mover el stock: inventario-service no responde";
		}
	}


	@Override
	public List<ItemOrdenTrabajo> getItems() {
		
		return itemRepository.findAll();
	}

	@Override
	public ItemOrdenTrabajo findItem(Long id) {
		
		return itemRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el item con id " + id));
	}

	@Override
	public String editItem(Long id, ItemOrdenTrabajo item) {
		
		ItemOrdenTrabajo it = this.findItem(id);
		
		// Los items que salieron del deposito no se editan. Si se cambiara la
		// cantidad aca, lo descontado del stock dejaria de coincidir con lo que
		// dice la orden. Para corregirlo hay que borrarlo (el repuesto vuelve
		// al stock) y cargarlo de nuevo con la cantidad correcta.
		if (it.getId_repuesto() != null) {
			throw new ReglaNegocioException("Este item salio del deposito y no se puede editar. Borralo (el repuesto vuelve al stock) y cargalo de nuevo.");
		}
		
		it.setTipo(item.getTipo());
		it.setDescripcion(item.getDescripcion());
		it.setCantidad(item.getCantidad());
		it.setPrecioUnitario(item.getPrecioUnitario());
		// el id_repuesto no se toca: un item cargado a mano no se puede
		// convertir en uno del deposito sin mover el stock
		
		itemRepository.save(it);
		
		return "Item actualizado correctamente";
	}

	@Override
	public String deleteItem(Long id) {
		
		ItemOrdenTrabajo item = itemRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el item con id " + id));
		
		// si era un repuesto del deposito, al sacarlo de la orden vuelve al stock
		if (item.getTipo() == TipoItem.REPUESTO && item.getId_repuesto() != null) {
			
			Long id_orden = item.getOrdenTrabajo() == null ? null : item.getOrdenTrabajo().getId_orden();
			
			String error = this.moverStock(item.getId_repuesto(), "ENTRADA", item.getCantidad(),
					"Devuelto al sacarlo de la orden " + id_orden, id_orden);
			
			if (error != null) {
				throw new ReglaNegocioException(error);
			}
		}
		
		itemRepository.deleteById(id);
		
		return "El Item fue eliminado correctamente";
	}

}
