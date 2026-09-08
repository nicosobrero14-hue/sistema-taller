package com.tallerapp.inventario_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.inventario_service.entity.MovimientoInventario;
import com.tallerapp.inventario_service.entity.Repuesto;
import com.tallerapp.inventario_service.entity.TipoMovimiento;
import com.tallerapp.inventario_service.exception.RecursoNoEncontradoException;
import com.tallerapp.inventario_service.exception.StockInsuficienteException;
import com.tallerapp.inventario_service.repository.IMovimientoInventarioRepository;
import com.tallerapp.inventario_service.repository.IRepuestoRepository;

@Service
public class MovimientoInventarioService implements IMovimientoInventarioService {
	
	@Autowired
	private IMovimientoInventarioRepository movimientoRepository;
	
	@Autowired
	private IRepuestoRepository repuestoRepository;
	
	
	// El stock del repuesto SOLO se cambia por aca. Cada vez que entra o sale
	// mercaderia queda el movimiento guardado, asi despues se puede ver por que
	// el stock es el que es.
	@Override
	public String saveMovimiento(Long id_repuesto, TipoMovimiento tipo, Integer cantidad, String motivo, Long id_orden, Long id_venta) {
		
		Repuesto repuesto = repuestoRepository.findById(id_repuesto)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el repuesto con id " + id_repuesto));
		
		int stockActual = repuesto.getStock() == null ? 0 : repuesto.getStock();
		int stockNuevo;
		
		if (tipo == TipoMovimiento.ENTRADA) {
			
			stockNuevo = stockActual + cantidad;
			
		} else if (tipo == TipoMovimiento.SALIDA) {
			
			if (cantidad > stockActual) {
				throw new StockInsuficienteException("No hay stock suficiente de " + repuesto.getNombre()
						+ ": quedan " + stockActual + " y se piden " + cantidad);
			}
			
			stockNuevo = stockActual - cantidad;
			
		} else {
			
			// AJUSTE: la cantidad es el stock real contado en el deposito
			stockNuevo = cantidad;
		}
		
		repuesto.setStock(stockNuevo);
		repuestoRepository.save(repuesto);
		
		MovimientoInventario movimiento = new MovimientoInventario();
		movimiento.setRepuesto(repuesto);
		movimiento.setTipo(tipo);
		movimiento.setCantidad(cantidad);
		movimiento.setStockResultante(stockNuevo);
		movimiento.setMotivo(motivo);
		movimiento.setFecha(LocalDateTime.now());
		movimiento.setId_orden(id_orden);
		movimiento.setId_venta(id_venta);
		
		movimientoRepository.save(movimiento);
		
		return "Movimiento registrado. " + repuesto.getNombre() + " queda con " + stockNuevo + " unidad(es)";
	}

	@Override
	public List<MovimientoInventario> getMovimientos() {
		
		return movimientoRepository.findAll();
	}

	@Override
	public MovimientoInventario findMovimiento(Long id) {
		
		return movimientoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el movimiento con id " + id));
	}

	@Override
	public List<MovimientoInventario> getMovimientosPorRepuesto(Long id_repuesto) {
		
		return movimientoRepository.findByRepuestoId(id_repuesto);
	}

}
