package com.tallerapp.pagos_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.pagos_service.client.ITallerClient;
import com.tallerapp.pagos_service.dto.OrdenDTO;
import com.tallerapp.pagos_service.entity.EstadoPago;
import com.tallerapp.pagos_service.entity.MedioPago;
import com.tallerapp.pagos_service.entity.Pago;
import com.tallerapp.pagos_service.exception.RecursoNoEncontradoException;
import com.tallerapp.pagos_service.exception.ReglaNegocioException;
import com.tallerapp.pagos_service.repository.IPagoRepository;

@Service
public class PagoService implements IPagoService {

	@Autowired
	private IPagoRepository pagoRepository;

	@Autowired
	private ITallerClient tallerClient;


	/**
	 * Registra el cobro de una orden. Se cobra todo junto, una sola vez:
	 * no hay señas. Antes le pregunta a taller-service cuanto suma la orden,
	 * para no depender de lo que mande el navegador.
	 *
	 * Queda PENDIENTE. Una transferencia se puede decir y no hacer, y una
	 * tarjeta puede rechazarse despues: la orden no cuenta como cobrada
	 * hasta que alguien mire la cuenta y lo confirme.
	 */
	@Override
	public String savePago(Long id_orden, BigDecimal monto, MedioPago medio, String observaciones, Long id_empleado) {

		if (this.findPagoPorOrden(id_orden) != null) {
			throw new ReglaNegocioException("La orden " + id_orden + " ya tiene un cobro cargado");
		}

		OrdenDTO orden = this.buscarOrden(id_orden);

		if (orden == null) {
			throw new ReglaNegocioException("No se encontro la orden " + id_orden + " en taller-service");
		}

		BigDecimal totalOrden = orden.getTotal() == null ? BigDecimal.ZERO : orden.getTotal();

		if (totalOrden.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("La orden " + id_orden + " no tiene items cargados, no hay nada que cobrar");
		}

		if (monto.compareTo(totalOrden) != 0) {
			throw new ReglaNegocioException("El monto no coincide: la orden suma $" + totalOrden + " y se quieren cobrar $" + monto);
		}

		Pago pago = new Pago();
		pago.setId_orden(id_orden);
		pago.setMonto(totalOrden);
		pago.setMedio(medio);
		pago.setObservaciones(observaciones);
		pago.setEstado(EstadoPago.PENDIENTE);
		pago.setFecha(LocalDateTime.now());
		pago.setId_empleado(id_empleado);

		pagoRepository.save(pago);

		return "Cobro de $" + totalOrden + " registrado en " + medio
				+ ". Falta confirmar que la plata entro";
	}

	/**
	 * Confirma que la plata entro de verdad. Recien aca se emite el recibo:
	 * el numero es correlativo, asi que no conviene gastar uno en un cobro
	 * que todavia se puede caer.
	 */
	@Override
	public String confirmarPago(Long id, Long id_empleado) {

		Pago pago = this.findPago(id);

		if (pago.getEstado() == EstadoPago.ANULADO) {
			throw new ReglaNegocioException("El cobro de la orden " + pago.getId_orden() + " esta anulado, no se puede confirmar");
		}

		if (pago.getEstado() == EstadoPago.CONFIRMADO) {
			throw new ReglaNegocioException("El cobro de la orden " + pago.getId_orden() + " ya estaba confirmado");
		}

		pago.setEstado(EstadoPago.CONFIRMADO);
		pago.setFechaConfirmacion(LocalDateTime.now());
		pago.setId_confirmo(id_empleado);
		pago.setNumeroRecibo(pagoRepository.ultimoNumeroRecibo() + 1);

		pagoRepository.save(pago);
		this.avisarAlTaller(pago.getId_orden(), true);

		return "Cobro confirmado. Recibo numero " + pago.getNumeroRecibo();
	}

	/**
	 * Anula un cobro. No lo borra: el numero de recibo queda quemado y la
	 * fila queda a la vista con el motivo, la fecha y quien la anulo. Un
	 * recibo que ya se imprimio no puede desaparecer del sistema.
	 *
	 * Despues de anular, la orden queda impaga y se puede volver a cobrar.
	 */
	@Override
	public String anularPago(Long id, String motivo, Long id_empleado) {

		Pago pago = this.findPago(id);

		if (pago.getEstado() == EstadoPago.ANULADO) {
			throw new ReglaNegocioException("El cobro de la orden " + pago.getId_orden() + " ya estaba anulado");
		}

		if (motivo == null || motivo.isBlank()) {
			throw new ReglaNegocioException("Hay que decir por que se anula el cobro");
		}

		pago.setEstado(EstadoPago.ANULADO);
		pago.setFechaAnulacion(LocalDateTime.now());
		pago.setId_anulo(id_empleado);
		pago.setMotivoAnulacion(motivo);

		pagoRepository.save(pago);
		this.avisarAlTaller(pago.getId_orden(), false);

		return "Se anulo el cobro de la orden " + pago.getId_orden()
				+ ". La orden vuelve a quedar impaga";
	}

	@Override
	public List<Pago> getPagos() {

		return pagoRepository.findAll();
	}

	@Override
	public Pago findPago(Long id) {

		return pagoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el pago con id " + id));
	}

	/**
	 * El cobro que vale de una orden: el ultimo que no este anulado.
	 * Devuelve null si la orden todavia no se cobro o si lo que habia
	 * se anulo.
	 */
	@Override
	public Pago findPagoPorOrden(Long id_orden) {

		for (Pago pago : pagoRepository.buscarPorOrden(id_orden)) {

			if (pago.getEstado() != EstadoPago.ANULADO) {
				return pago;
			}
		}

		return null;
	}

	/**
	 * Todos los cobros de una orden, anulados incluidos. Es lo que mira la
	 * pantalla de cobro para mostrar "esto se anulo y por que".
	 */
	@Override
	public List<Pago> getPagosDeOrden(Long id_orden) {

		return pagoRepository.buscarPorOrden(id_orden);
	}

	// Si taller-service no contesta, la orden queda sin marcar: no se va a
	// poder entregar hasta que alguien vuelva a confirmar. Es el lado
	// barato del error.
	private void avisarAlTaller(Long id_orden, boolean pagada) {

		try {
			tallerClient.marcarPagada(id_orden, pagada);
		} catch (Exception e) {
			// nada que hacer desde aca
		}
	}

	// devuelve null si taller-service no responde
	private OrdenDTO buscarOrden(Long id_orden) {

		try {
			return tallerClient.traerOrden(id_orden);
		} catch (Exception e) {
			return null;
		}
	}

}
