package com.tallerapp.pagos_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tallerapp.pagos_service.client.ITallerClient;
import com.tallerapp.pagos_service.dto.OrdenDTO;
import com.tallerapp.pagos_service.entity.EstadoPago;
import com.tallerapp.pagos_service.entity.MedioPago;
import com.tallerapp.pagos_service.entity.Pago;
import com.tallerapp.pagos_service.exception.ReglaNegocioException;
import com.tallerapp.pagos_service.repository.IPagoRepository;

/**
 * Las reglas de la plata. Son las que no se pueden romper sin enterarse:
 * si alguna de estas se cae, el taller cobra de menos o entrega gratis.
 */
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

	@Mock
	private IPagoRepository pagoRepository;

	@Mock
	private ITallerClient tallerClient;

	@InjectMocks
	private PagoService pagoService;


	// arma una orden como la devuelve taller-service
	private OrdenDTO orden(String total) {

		OrdenDTO orden = new OrdenDTO();
		orden.setTotal(new BigDecimal(total));

		return orden;
	}

	private Pago pagoGuardado(EstadoPago estado, Long numeroRecibo) {

		Pago pago = new Pago();
		pago.setId_pago(1L);
		pago.setId_orden(10L);
		pago.setMonto(new BigDecimal("50000"));
		pago.setMedio(MedioPago.TRANSFERENCIA);
		pago.setEstado(estado);
		pago.setNumeroRecibo(numeroRecibo);

		return pago;
	}


	@Test
	void elCobroNuevoQuedaPendienteYSinRecibo() {

		when(pagoRepository.buscarPorOrden(10L)).thenReturn(List.of());
		when(tallerClient.traerOrden(10L)).thenReturn(this.orden("50000"));

		String resultado = pagoService.savePago(10L, new BigDecimal("50000"), MedioPago.TRANSFERENCIA, "", 2L);

		ArgumentCaptor<Pago> guardado = ArgumentCaptor.forClass(Pago.class);
		verify(pagoRepository).save(guardado.capture());

		assertEquals(EstadoPago.PENDIENTE, guardado.getValue().getEstado());
		assertNull(guardado.getValue().getNumeroRecibo(), "el recibo se emite al confirmar, no al cargar");
		assertTrue(resultado.contains("Falta confirmar"));
	}

	@Test
	void noCobraSiElMontoNoCoincideConElTotalDeLaOrden() {

		when(pagoRepository.buscarPorOrden(10L)).thenReturn(List.of());
		when(tallerClient.traerOrden(10L)).thenReturn(this.orden("50000"));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> pagoService.savePago(10L, new BigDecimal("500"), MedioPago.EFECTIVO, "", 2L));

		verify(pagoRepository, never()).save(any());
		assertTrue(e.getMessage().contains("no coincide"), e.getMessage());
	}

	@Test
	void noCobraDosVecesLaMismaOrden() {

		when(pagoRepository.buscarPorOrden(10L)).thenReturn(List.of(this.pagoGuardado(EstadoPago.PENDIENTE, null)));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> pagoService.savePago(10L, new BigDecimal("50000"), MedioPago.EFECTIVO, "", 2L));

		verify(pagoRepository, never()).save(any());
		assertTrue(e.getMessage().contains("ya tiene un cobro cargado"), e.getMessage());
	}

	@Test
	void confirmarEmiteElReciboYAnotaQuienFue() {

		when(pagoRepository.findById(1L)).thenReturn(java.util.Optional.of(this.pagoGuardado(EstadoPago.PENDIENTE, null)));
		when(pagoRepository.ultimoNumeroRecibo()).thenReturn(7L);

		String resultado = pagoService.confirmarPago(1L, 3L);

		ArgumentCaptor<Pago> guardado = ArgumentCaptor.forClass(Pago.class);
		verify(pagoRepository).save(guardado.capture());

		assertEquals(EstadoPago.CONFIRMADO, guardado.getValue().getEstado());
		assertEquals(8L, guardado.getValue().getNumeroRecibo());
		assertEquals(3L, guardado.getValue().getId_confirmo());
		assertNotNull(guardado.getValue().getFechaConfirmacion());
		assertTrue(resultado.contains("Recibo numero 8"), resultado);
	}

	@Test
	void confirmarDosVecesNoGastaOtroNumeroDeRecibo() {

		when(pagoRepository.findById(1L)).thenReturn(java.util.Optional.of(this.pagoGuardado(EstadoPago.CONFIRMADO, 8L)));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> pagoService.confirmarPago(1L, 3L));

		verify(pagoRepository, never()).save(any());
		assertTrue(e.getMessage().contains("ya estaba confirmado"), e.getMessage());
	}

	@Test
	void anularNoBorraElReciboYPideMotivo() {

		when(pagoRepository.findById(1L)).thenReturn(java.util.Optional.of(this.pagoGuardado(EstadoPago.CONFIRMADO, 8L)));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> pagoService.anularPago(1L, "  ", 3L));

		verify(pagoRepository, never()).save(any());
		assertTrue(e.getMessage().contains("por que se anula"), e.getMessage());

		pagoService.anularPago(1L, "El cliente rechazo la transferencia", 3L);

		ArgumentCaptor<Pago> guardado = ArgumentCaptor.forClass(Pago.class);
		verify(pagoRepository).save(guardado.capture());

		assertEquals(EstadoPago.ANULADO, guardado.getValue().getEstado());
		assertEquals(8L, guardado.getValue().getNumeroRecibo(), "el numero de recibo queda quemado, no se recicla");
		assertEquals("El cliente rechazo la transferencia", guardado.getValue().getMotivoAnulacion());
	}

	@Test
	void despuesDeAnularLaOrdenSePuedeVolverACobrar() {

		// la orden tiene un cobro, pero anulado: es como si no tuviera
		when(pagoRepository.buscarPorOrden(10L)).thenReturn(List.of(this.pagoGuardado(EstadoPago.ANULADO, 8L)));
		when(tallerClient.traerOrden(10L)).thenReturn(this.orden("50000"));

		String resultado = pagoService.savePago(10L, new BigDecimal("50000"), MedioPago.EFECTIVO, "", 2L);

		verify(pagoRepository).save(any());
		assertTrue(resultado.contains("registrado"), resultado);
	}

	@Test
	void noSePuedeCobrarUnaOrdenSinItems() {

		when(pagoRepository.buscarPorOrden(10L)).thenReturn(List.of());
		when(tallerClient.traerOrden(10L)).thenReturn(this.orden("0"));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> pagoService.savePago(10L, BigDecimal.ZERO, MedioPago.EFECTIVO, "", 2L));

		verify(pagoRepository, never()).save(any());
		assertTrue(e.getMessage().contains("no tiene items"), e.getMessage());
	}

}
