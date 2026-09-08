package com.tallerapp.taller_service.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.taller_service.client.IClientesClient;
import com.tallerapp.taller_service.client.IInventarioClient;
import com.tallerapp.taller_service.client.IUsuariosClient;
import com.tallerapp.taller_service.dto.EmpleadoDTO;
import com.tallerapp.taller_service.dto.MovimientoDTO;
import com.tallerapp.taller_service.dto.SucursalDTO;
import com.tallerapp.taller_service.dto.VehiculoDTO;
import com.tallerapp.taller_service.entity.EstadoOrden;
import com.tallerapp.taller_service.entity.ItemOrdenTrabajo;
import com.tallerapp.taller_service.entity.MovimientoOrden;
import com.tallerapp.taller_service.entity.OrdenTrabajo;
import com.tallerapp.taller_service.entity.TipoItem;
import com.tallerapp.taller_service.exception.RecursoNoEncontradoException;
import com.tallerapp.taller_service.exception.ReglaNegocioException;
import com.tallerapp.taller_service.repository.IMovimientoOrdenRepository;
import com.tallerapp.taller_service.repository.IOrdenTrabajoRepository;

@Service
public class OrdenTrabajoService implements IOrdenTrabajoService {
	
	@Autowired
	private IOrdenTrabajoRepository ordenRepository;
	
	@Autowired
	private IClientesClient clientesClient;
	
	@Autowired
	private IUsuariosClient usuariosClient;
	
	@Autowired
	private IInventarioClient inventarioClient;
	
	@Autowired
	private IMovimientoOrdenRepository movimientoRepository;
	
	
	@Override
	public String saveOrden(Long id_vehiculo, Long id_mecanico, Long id_sucursal, String diagnostico, LocalDateTime fechaEntregaEstimada, Integer kilometrajeIngreso, Long id_empleado) {
		
		VehiculoDTO vehiculo = this.buscarVehiculo(id_vehiculo);
		
		if (vehiculo == null) {
			throw new ReglaNegocioException("No se encontro el vehiculo " + id_vehiculo + " en clientes-service");
		}
		
		EmpleadoDTO mecanico = this.buscarMecanico(id_mecanico);
		
		if (mecanico == null) {
			throw new ReglaNegocioException("No se encontro el mecanico " + id_mecanico + " en usuarios-service");
		}
		
		SucursalDTO sucursal = this.buscarSucursal(id_sucursal);
		
		if (sucursal == null) {
			throw new ReglaNegocioException("No se encontro la sucursal " + id_sucursal + " en usuarios-service");
		}
		
		OrdenTrabajo orden = new OrdenTrabajo();
		orden.setId_vehiculo(id_vehiculo);
		orden.setId_mecanico(id_mecanico);
		orden.setId_sucursal(id_sucursal);
		orden.setEstado(EstadoOrden.RECIBIDO);
		orden.setDiagnostico(diagnostico);
		orden.setKilometrajeIngreso(kilometrajeIngreso);
		orden.setFechaIngreso(LocalDateTime.now());
		orden.setFechaEntregaEstimada(fechaEntregaEstimada);
		
		ordenRepository.save(orden);
		
		this.anotarMovimiento(orden, null, EstadoOrden.RECIBIDO, id_empleado, "Orden creada");
		
		return "Orden de trabajo creada correctamente para el vehiculo " + vehiculo.getPatente();
	}
	
	// consulta a clientes-service. devuelve null si el vehiculo no existe
	// o si el servicio no esta levantado
	private VehiculoDTO buscarVehiculo(Long id_vehiculo) {
		
		try {
			return clientesClient.traerVehiculo(id_vehiculo);
		} catch (Exception e) {
			return null;
		}
	}
	
	// consulta a usuarios-service, mismo criterio
	private EmpleadoDTO buscarMecanico(Long id_mecanico) {
		
		try {
			return usuariosClient.traerEmpleado(id_mecanico);
		} catch (Exception e) {
			return null;
		}
	}
	
	private SucursalDTO buscarSucursal(Long id_sucursal) {
		
		try {
			return usuariosClient.traerSucursal(id_sucursal);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Las ordenes que siguen en el taller. Es lo que se ve todos los dias:
	 * las entregadas se van al archivo y no ensucian la pantalla.
	 */
	@Override
	public List<OrdenTrabajo> getAbiertas() {

		return this.conTotal(ordenRepository.buscarAbiertas());
	}

	/**
	 * El archivo: las entregadas entre dos fechas. Se pide por pedazos
	 * porque en un par de años son miles.
	 */
	@Override
	public List<OrdenTrabajo> getEntregadas(LocalDate desde, LocalDate hasta) {

		return this.conTotal(ordenRepository.buscarEntregadas(desde.atStartOfDay(),
															  hasta.plusDays(1).atStartOfDay()));
	}

	// le calcula el total a cada una antes de devolverlas
	private List<OrdenTrabajo> conTotal(List<OrdenTrabajo> ordenes) {

		for (OrdenTrabajo orden : ordenes) {
			this.calcularTotal(orden);
		}

		return ordenes;
	}

	@Override
	public List<OrdenTrabajo> getOrdenes() {
		
		List<OrdenTrabajo> ordenes = ordenRepository.findAll();
		
		for (OrdenTrabajo orden : ordenes) {
			this.calcularTotal(orden);
		}
		
		return ordenes;
	}

	@Override
	public OrdenTrabajo findOrden(Long id) {
		
		OrdenTrabajo orden = ordenRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden con id " + id));
		
		this.calcularTotal(orden);
		
		return orden;
	}
	
	@Override
	public Long contarPorVehiculo(Long id_vehiculo) {
		
		return ordenRepository.contarPorVehiculo(id_vehiculo);
	}
	
	@Override
	public Long contarPorMecanico(Long id_mecanico) {
		
		return ordenRepository.contarPorMecanico(id_mecanico);
	}
	
	//5- historial: todas las ordenes que tuvo un vehiculo
	@Override
	public List<OrdenTrabajo> getOrdenesPorVehiculo(Long id_vehiculo) {
		
		List<OrdenTrabajo> ordenes = ordenRepository.findByVehiculoId(id_vehiculo);
		
		for (OrdenTrabajo orden : ordenes) {
			this.calcularTotal(orden);
		}
		
		return ordenes;
	}
	
	//6- cambiar el estado. Los estados son libres (se puede ir a cualquiera,
	// incluso reabrir una orden ya entregada si el vehiculo vuelve por lo
	// mismo) salvo uno: el vehiculo no sale del taller sin estar cobrado.
	@Override
	public String cambiarEstado(Long id, EstadoOrden estado, Long id_empleado) {
		
		OrdenTrabajo orden = ordenRepository.findById(id).orElse(null);
		
		if (orden == null) {
			throw new ReglaNegocioException("No se encontro la orden " + id);
		}
		
		if (estado == EstadoOrden.ENTREGADO && !Boolean.TRUE.equals(orden.getPagada())) {
			throw new ReglaNegocioException("La orden " + id + " no se puede entregar: el cobro todavia no esta confirmado");
		}
		
		EstadoOrden estadoAnterior = orden.getEstado();
		
		orden.setEstado(estado);
		
		if (estado == EstadoOrden.ENTREGADO) {
			orden.setFechaEntregaReal(LocalDateTime.now());
		} else {
			// si se reabre, la fecha de entrega deja de valer
			orden.setFechaEntregaReal(null);
		}
		
		ordenRepository.save(orden);
		
		this.anotarMovimiento(orden, estadoAnterior, estado, id_empleado, null);
		
		return "La orden " + id + " paso a " + estado;
	}
	
	// La llama pagos-service cuando confirma o anula un cobro.
	@Override
	public void marcarPagada(Long id, boolean pagada) {

		OrdenTrabajo orden = ordenRepository.findById(id).orElse(null);

		if (orden != null) {
			orden.setPagada(pagada);
			ordenRepository.save(orden);
		}
	}

	//7- la linea de tiempo de la orden: quien la movio y cuando
	@Override
	public List<MovimientoOrden> getMovimientos(Long id_orden) {
		
		return movimientoRepository.buscarPorOrden(id_orden);
	}
	
	// Deja anotado el cambio. Se llama despues de guardar: si el guardado
	// falla no queda anotado un movimiento de algo que nunca paso.
	private void anotarMovimiento(OrdenTrabajo orden, EstadoOrden anterior, EstadoOrden nuevo, Long id_empleado, String detalle) {
		
		MovimientoOrden movimiento = new MovimientoOrden();
		movimiento.setOrdenTrabajo(orden);
		movimiento.setEstadoAnterior(anterior);
		movimiento.setEstadoNuevo(nuevo);
		movimiento.setFecha(LocalDateTime.now());
		movimiento.setId_empleado(id_empleado);
		movimiento.setDetalle(detalle);
		
		movimientoRepository.save(movimiento);
	}
	
	
	// suma cantidad * precio de cada item de la orden
	private void calcularTotal(OrdenTrabajo orden) {
		
		BigDecimal total = BigDecimal.ZERO;
		
		if (orden.getItems() != null) {
			
			for (ItemOrdenTrabajo item : orden.getItems()) {
				
				if (item.getCantidad() != null && item.getPrecioUnitario() != null) {
					total = total.add(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())));
				}
			}
		}
		
		BigDecimal descuento = orden.getDescuento() == null ? BigDecimal.ZERO : orden.getDescuento();
		
		orden.setSubtotal(total);
		orden.setTotal(total.subtract(descuento).max(BigDecimal.ZERO));
	}

	@Override
	public String editOrden(Long id, OrdenTrabajo orden) {
		
		OrdenTrabajo ord = ordenRepository.findById(id).orElse(null);
		
		if (ord == null) {
			throw new ReglaNegocioException("No se encontro la orden " + id);
		}
		
		// las mismas validaciones que al crear: los ids tienen que existir
		// en los otros servicios
		if (this.buscarVehiculo(orden.getId_vehiculo()) == null) {
			throw new ReglaNegocioException("No se encontro el vehiculo " + orden.getId_vehiculo() + " en clientes-service");
		}
		
		if (this.buscarMecanico(orden.getId_mecanico()) == null) {
			throw new ReglaNegocioException("No se encontro el mecanico " + orden.getId_mecanico() + " en usuarios-service");
		}
		
		if (this.buscarSucursal(orden.getId_sucursal()) == null) {
			throw new ReglaNegocioException("No se encontro la sucursal " + orden.getId_sucursal() + " en usuarios-service");
		}
		
		ord.setId_vehiculo(orden.getId_vehiculo());
		ord.setId_mecanico(orden.getId_mecanico());
		ord.setId_sucursal(orden.getId_sucursal());
		ord.setDiagnostico(orden.getDiagnostico());
		ord.setKilometrajeIngreso(orden.getKilometrajeIngreso());
		ord.setFechaEntregaEstimada(orden.getFechaEntregaEstimada());
		ord.setDescuento(orden.getDescuento());
		// el estado no se toca aca: para eso esta cambiarEstado
		
		ordenRepository.save(ord);
		
		return "Orden " + id + " actualizada correctamente";
	}

	@Override
	public String deleteOrden(Long id) {
		
		OrdenTrabajo orden = ordenRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden con id " + id));
		
		// Una orden cobrada no se borra: quedaria un recibo entregado al
		// cliente apuntando a una orden que ya no existe. Primero se anula
		// el cobro y despues se borra.
		if (Boolean.TRUE.equals(orden.getPagada())) {
			throw new ReglaNegocioException("La orden " + id + " esta cobrada. Anula el cobro antes de borrarla");
		}
		
		// Los repuestos que se usaron vuelven al deposito. Sacarlos de a
		// uno ya los devolvia; borrar la orden entera no, y el stock
		// quedaba descontado para siempre.
		if (orden.getItems() != null) {
		
			for (ItemOrdenTrabajo item : orden.getItems()) {
				
				if (item.getTipo() == TipoItem.REPUESTO && item.getId_repuesto() != null) {
					this.devolverAlDeposito(item, id);
				}
			}
		}
		
		// los items y los movimientos se borran solos por el cascade
		ordenRepository.deleteById(id);
		
		return "La orden " + id + " fue eliminada correctamente";
	}
	
	// Si el deposito no contesta se sigue igual: dejar una orden sin
	// poder borrar porque inventario-service esta caido es peor.
	private void devolverAlDeposito(ItemOrdenTrabajo item, Long id_orden) {
		
		MovimientoDTO movimiento = new MovimientoDTO();
		movimiento.setId_repuesto(item.getId_repuesto());
		movimiento.setTipo("ENTRADA");
		movimiento.setCantidad(item.getCantidad());
		movimiento.setMotivo("Devuelto al borrar la orden " + id_orden);
		movimiento.setId_orden(id_orden);
		
		try {
			inventarioClient.moverStock(movimiento);
		} catch (Exception e) {
			// no se pudo anotar el movimiento; el borrado sigue igual
		}
	}

}
