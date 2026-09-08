package com.tallerapp.inventario_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.inventario_service.entity.PrecioRepuesto;
import com.tallerapp.inventario_service.entity.Repuesto;
import com.tallerapp.inventario_service.exception.RecursoNoEncontradoException;
import com.tallerapp.inventario_service.exception.ReglaNegocioException;
import com.tallerapp.inventario_service.repository.IMovimientoInventarioRepository;
import com.tallerapp.inventario_service.repository.IPrecioRepuestoRepository;
import com.tallerapp.inventario_service.repository.IRepuestoRepository;

@Service
public class RepuestoService implements IRepuestoService {
	
	@Autowired
	private IRepuestoRepository repuestoRepository;
	
	@Autowired
	private IMovimientoInventarioRepository movimientoRepository;
	
	@Autowired
	private IPrecioRepuestoRepository precioRepository;
	
	
	@Override
	public String saveRepuesto(String codigo, String codigoBarra, String nombre, String descripcion,
							   String marca, String ubicacion, BigDecimal precioCompra, BigDecimal precioVenta,
							   Integer stock, Integer stockMinimo, Long id_sucursal) {
		
		// los dos codigos son unicos: si ya existe uno, avisamos en vez de
		// dejar que reviente la base
		if (repuestoRepository.findByCodigo(codigo).isPresent()) {
			throw new ReglaNegocioException("Ya existe un repuesto con el codigo " + codigo);
		}
		
		if (codigoBarra != null && !codigoBarra.isBlank()
				&& repuestoRepository.findByCodigoBarra(codigoBarra).isPresent()) {
			throw new ReglaNegocioException("Ya existe un repuesto con el codigo de barras " + codigoBarra);
		}
		
		Repuesto repuesto = new Repuesto();
		repuesto.setCodigo(codigo);
		repuesto.setCodigoBarra(this.limpiar(codigoBarra));
		repuesto.setNombre(nombre);
		repuesto.setDescripcion(descripcion);
		repuesto.setMarca(marca);
		repuesto.setUbicacion(ubicacion);
		repuesto.setPrecioCompra(precioCompra);
		repuesto.setPrecioVenta(precioVenta);
		repuesto.setStock(stock == null ? 0 : stock);
		repuesto.setStockMinimo(stockMinimo == null ? 0 : stockMinimo);
		repuesto.setId_sucursal(id_sucursal);
		
		repuestoRepository.save(repuesto);
		
		this.guardarPrecio(repuesto, null);
		
		return "Repuesto creado correctamente";
	}

	@Override
	public List<Repuesto> getRepuestos() {
		
		return repuestoRepository.findAll();
	}

	@Override
	public Repuesto findRepuesto(Long id) {
		
		return repuestoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el repuesto con id " + id));
	}

	@Override
	public String editRepuesto(Long id, Repuesto repuesto, Long id_empleado) {
		
		Repuesto rep = this.findRepuesto(id);
		
		// si le cambiaron el codigo, que no pise el de otro repuesto
		if (this.codigoOcupado(repuestoRepository.findByCodigo(repuesto.getCodigo()), id)) {
			throw new ReglaNegocioException("Ya existe otro repuesto con el codigo " + repuesto.getCodigo());
		}
		
		if (repuesto.getCodigoBarra() != null && !repuesto.getCodigoBarra().isBlank()
				&& this.codigoOcupado(repuestoRepository.findByCodigoBarra(repuesto.getCodigoBarra()), id)) {
			throw new ReglaNegocioException("Ya existe otro repuesto con el codigo de barras " + repuesto.getCodigoBarra());
		}
		
		// hay que mirar el precio viejo antes de pisarlo con el nuevo
		boolean cambioPrecio = this.distinto(rep.getPrecioVenta(), repuesto.getPrecioVenta())
				|| this.distinto(rep.getPrecioCompra(), repuesto.getPrecioCompra());
		
		rep.setCodigo(repuesto.getCodigo());
		rep.setCodigoBarra(this.limpiar(repuesto.getCodigoBarra()));
		rep.setNombre(repuesto.getNombre());
		rep.setDescripcion(repuesto.getDescripcion());
		rep.setMarca(repuesto.getMarca());
		rep.setUbicacion(repuesto.getUbicacion());
		rep.setPrecioCompra(repuesto.getPrecioCompra());
		rep.setPrecioVenta(repuesto.getPrecioVenta());
		rep.setStockMinimo(repuesto.getStockMinimo());
		rep.setId_sucursal(repuesto.getId_sucursal());
		// el stock no se toca aca: se cambia con un movimiento, para que
		// siempre quede registrado quien lo movio y por que
		
		repuestoRepository.save(rep);
		
		if (cambioPrecio) {
			this.guardarPrecio(rep, id_empleado);
		}
		
		return "Repuesto actualizado correctamente";
	}

	@Override
	public String deleteRepuesto(Long id) {
		
		this.findRepuesto(id);
		
		// si tuvo movimientos no se borra: se perderia el historial del deposito
		int movimientos = movimientoRepository.findByRepuestoId(id).size();
		
		if (movimientos > 0) {
			throw new ReglaNegocioException("No se puede borrar: el repuesto tiene " + movimientos + " movimiento(s) registrado(s)");
		}
		
		repuestoRepository.deleteById(id);
		
		return "El Repuesto fue eliminado correctamente";
	}

	@Override
	public Repuesto findPorCodigoBarra(String codigoBarra) {
		
		return repuestoRepository.findByCodigoBarra(codigoBarra)
				.orElseThrow(() -> new RecursoNoEncontradoException("Ningun repuesto tiene el codigo " + codigoBarra));
	}

	@Override
	public List<Repuesto> buscar(String texto) {
		
		if (texto == null || texto.isBlank()) {
			return repuestoRepository.findAll();
		}
		
		return repuestoRepository.buscar(texto);
	}

	@Override
	public List<Repuesto> getBajoStock() {
		
		return repuestoRepository.findBajoStock();
	}
	
	@Override
	public List<PrecioRepuesto> getHistorialPrecios(Long id_repuesto) {
		
		this.findRepuesto(id_repuesto);
		
		return precioRepository.findByRepuestoId(id_repuesto);
	}
	
	// deja una foto del precio de hoy
	private void guardarPrecio(Repuesto repuesto, Long id_empleado) {
		
		PrecioRepuesto precio = new PrecioRepuesto();
		precio.setRepuesto(repuesto);
		precio.setPrecioCompra(repuesto.getPrecioCompra());
		precio.setPrecioVenta(repuesto.getPrecioVenta());
		precio.setFecha(LocalDateTime.now());
		precio.setId_empleado(id_empleado);
		
		precioRepository.save(precio);
	}
	
	private boolean distinto(BigDecimal uno, BigDecimal otro) {
		
		if (uno == null || otro == null) {
			return uno != otro;
		}
		
		return uno.compareTo(otro) != 0;
	}
	
	// el codigo de barras vacio se guarda como null y no como "",
	// asi la restriccion de unico no choca entre varios repuestos sin codigo
	private String limpiar(String codigoBarra) {
		
		if (codigoBarra == null || codigoBarra.isBlank()) {
			return null;
		}
		
		return codigoBarra.trim();
	}
	
	// true si ese codigo ya lo tiene otro repuesto distinto del que estamos editando
	private boolean codigoOcupado(java.util.Optional<Repuesto> encontrado, Long id) {
		
		return encontrado.isPresent() && !encontrado.get().getId_repuesto().equals(id);
	}

}
