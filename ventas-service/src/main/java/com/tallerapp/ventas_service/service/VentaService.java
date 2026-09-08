package com.tallerapp.ventas_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.ventas_service.client.IInventarioClient;
import com.tallerapp.ventas_service.dto.DetalleVentaDTO;
import com.tallerapp.ventas_service.dto.MovimientoDTO;
import com.tallerapp.ventas_service.dto.RepuestoDTO;
import com.tallerapp.ventas_service.dto.VentaDTO;
import com.tallerapp.ventas_service.entity.DetalleVenta;
import com.tallerapp.ventas_service.entity.EstadoVenta;
import com.tallerapp.ventas_service.entity.Venta;
import com.tallerapp.ventas_service.exception.RecursoNoEncontradoException;
import com.tallerapp.ventas_service.exception.ReglaNegocioException;
import com.tallerapp.ventas_service.repository.IVentaRepository;

@Service
public class VentaService implements IVentaService {

	@Autowired
	private IVentaRepository ventaRepository;

	@Autowired
	private IInventarioClient inventarioClient;


	/**
	 * Vende repuestos por mostrador.
	 *
	 * El navegador manda que repuestos y cuantos; el precio lo pone el
	 * deposito. Primero se fija que haya stock de todo y recien despues
	 * descuenta: nadie se lleva media venta.
	 */
	@Override
	public String saveVenta(VentaDTO ventaDTO, Long id_empleado) {

		List<RepuestoDTO> repuestos = new ArrayList<>();

		for (DetalleVentaDTO detalle : ventaDTO.getDetalles()) {

			RepuestoDTO repuesto = this.buscarRepuesto(detalle.getId_repuesto());

			if (repuesto == null) {
				throw new ReglaNegocioException("No se encontro el repuesto " + detalle.getId_repuesto() + " en el deposito");
			}

			if (repuesto.getStock() == null || repuesto.getStock() < detalle.getCantidad()) {
				throw new ReglaNegocioException("No hay stock de " + repuesto.getNombre() + ": quedan "
						+ repuesto.getStock() + " y se quieren vender " + detalle.getCantidad());
			}

			repuestos.add(repuesto);
		}

		Venta venta = new Venta();
		venta.setEstado(EstadoVenta.HECHA);
		venta.setMedio(ventaDTO.getMedio());
		venta.setId_cliente(ventaDTO.getId_cliente());
		venta.setNombreCliente(ventaDTO.getNombreCliente());
		venta.setId_sucursal(ventaDTO.getId_sucursal());
		venta.setId_empleado(id_empleado);
		venta.setObservaciones(ventaDTO.getObservaciones());
		venta.setFecha(LocalDateTime.now());
		venta.setNumeroComprobante(ventaRepository.ultimoNumeroComprobante() + 1);
		venta.setDetalles(new ArrayList<>());

		for (int i = 0; i < repuestos.size(); i++) {

			RepuestoDTO repuesto = repuestos.get(i);

			DetalleVenta detalle = new DetalleVenta();
			detalle.setVenta(venta);
			detalle.setId_repuesto(repuesto.getId_repuesto());
			detalle.setDescripcion(repuesto.getNombre()
					+ (repuesto.getMarca() != null ? " (" + repuesto.getMarca() + ")" : ""));
			detalle.setCantidad(ventaDTO.getDetalles().get(i).getCantidad());
			detalle.setPrecioUnitario(repuesto.getPrecioVenta());

			venta.getDetalles().add(detalle);
		}

		ventaRepository.save(venta);

		// Recien con la venta guardada se toca el stock, asi el movimiento
		// puede decir de que venta salio. Si alguno falla igual, se devuelve
		// lo que ya habia salido y se borra la venta: no queda plata cobrada
		// contra un deposito que no se movio.
		String problema = this.moverStock(venta, "SALIDA", "Venta " + venta.getNumeroComprobante());

		if (problema != null) {
			ventaRepository.delete(venta);
			throw new ReglaNegocioException(problema);
		}

		this.calcularTotal(venta);

		return "Venta " + venta.getNumeroComprobante() + " registrada por $" + venta.getTotal();
	}

	/**
	 * Anula una venta y devuelve los repuestos al deposito. No se borra:
	 * el numero de comprobante queda usado y el motivo a la vista.
	 */
	@Override
	public String anularVenta(Long id, String motivo, Long id_empleado) {

		Venta venta = this.findVenta(id);

		if (venta.getEstado() == EstadoVenta.ANULADA) {
			throw new ReglaNegocioException("La venta " + venta.getNumeroComprobante() + " ya estaba anulada");
		}

		if (motivo == null || motivo.isBlank()) {
			throw new ReglaNegocioException("Hay que decir por que se anula la venta");
		}

		// Primero se marca como anulada y despues se devuelve el stock. Al
		// reves, si el guardado fallara despues de devolver, la venta
		// quedaria HECHA y se podria anular otra vez: el deposito sumaria
		// los repuestos dos veces.
		venta.setEstado(EstadoVenta.ANULADA);
		venta.setFechaAnulacion(LocalDateTime.now());
		venta.setId_anulo(id_empleado);
		venta.setMotivoAnulacion(motivo);

		ventaRepository.save(venta);

		String problema = this.moverStock(venta, "ENTRADA",
				"Anulacion de la venta " + venta.getNumeroComprobante());

		if (problema != null) {

			venta.setEstado(EstadoVenta.HECHA);
			venta.setFechaAnulacion(null);
			venta.setId_anulo(null);
			venta.setMotivoAnulacion(null);

			ventaRepository.save(venta);

			throw new ReglaNegocioException(problema);
		}

		return "Venta " + venta.getNumeroComprobante() + " anulada. Los repuestos volvieron al deposito";
	}

	@Override
	public List<Venta> getVentas() {

		List<Venta> ventas = ventaRepository.traerOrdenadas();

		for (Venta venta : ventas) {
			this.calcularTotal(venta);
		}

		return ventas;
	}

	@Override
	public Venta findVenta(Long id) {

		Venta venta = ventaRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la venta con id " + id));

		this.calcularTotal(venta);

		return venta;
	}

	// suma cantidad * precio de cada renglon
	private void calcularTotal(Venta venta) {

		BigDecimal total = BigDecimal.ZERO;

		if (venta.getDetalles() != null) {

			for (DetalleVenta detalle : venta.getDetalles()) {
				total = total.add(detalle.getPrecioUnitario()
						.multiply(BigDecimal.valueOf(detalle.getCantidad())));
			}
		}

		venta.setTotal(total);
	}

	/**
	 * Mueve el stock de todos los renglones para el mismo lado.
	 *
	 * Devuelve null si salio todo bien, o el mensaje del problema. Si falla
	 * en el medio deshace lo que ya habia movido: media venta descontada es
	 * peor que ninguna.
	 */
	private String moverStock(Venta venta, String tipo, String motivo) {

		String contrario = "SALIDA".equals(tipo) ? "ENTRADA" : "SALIDA";
		List<DetalleVenta> hechos = new ArrayList<>();

		for (DetalleVenta detalle : venta.getDetalles()) {

			try {
				inventarioClient.moverStock(this.movimiento(detalle, tipo, motivo, venta.getId_venta()));
				hechos.add(detalle);

			} catch (Exception e) {

				for (DetalleVenta hecho : hechos) {
					this.moverCallado(hecho, contrario, "Se deshizo: " + motivo, venta.getId_venta());
				}

				return "No se pudo mover el stock de " + detalle.getDescripcion()
						+ ". No se toco nada del deposito";
			}
		}

		return null;
	}

	private MovimientoDTO movimiento(DetalleVenta detalle, String tipo, String motivo, Long id_venta) {

		MovimientoDTO movimiento = new MovimientoDTO();
		movimiento.setId_repuesto(detalle.getId_repuesto());
		movimiento.setTipo(tipo);
		movimiento.setCantidad(detalle.getCantidad());
		movimiento.setMotivo(motivo);
		movimiento.setId_venta(id_venta);

		return movimiento;
	}

	// para deshacer: si esto tambien falla no hay nada mejor que hacer que
	// seguir, el mensaje de error ya se le va a mostrar al que vendio
	private void moverCallado(DetalleVenta detalle, String tipo, String motivo, Long id_venta) {

		try {
			inventarioClient.moverStock(this.movimiento(detalle, tipo, motivo, id_venta));
		} catch (Exception e) {
			// no hay a quien avisarle desde aca
		}
	}

	// devuelve null si inventario-service no responde o el repuesto no existe
	private RepuestoDTO buscarRepuesto(Long id_repuesto) {

		try {
			return inventarioClient.traerRepuesto(id_repuesto);
		} catch (Exception e) {
			return null;
		}
	}

}
