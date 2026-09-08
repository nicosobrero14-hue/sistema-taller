package com.tallerapp.taller_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tallerapp.taller_service.entity.EstadoOrden;
import com.tallerapp.taller_service.entity.ItemOrdenTrabajo;
import com.tallerapp.taller_service.entity.OrdenTrabajo;
import com.tallerapp.taller_service.exception.ReglaNegocioException;
import com.tallerapp.taller_service.repository.IMovimientoOrdenRepository;
import com.tallerapp.taller_service.repository.IOrdenTrabajoRepository;

/**
 * La regla que protege la plata del taller: el vehiculo no sale sin estar
 * cobrado. Todo lo demas de los estados es libre a proposito.
 */
@ExtendWith(MockitoExtension.class)
class OrdenTrabajoServiceTest {

	@Mock
	private IOrdenTrabajoRepository ordenRepository;

	@Mock
	private IMovimientoOrdenRepository movimientoRepository;

	@InjectMocks
	private OrdenTrabajoService ordenService;


	private OrdenTrabajo ordenLista() {

		OrdenTrabajo orden = new OrdenTrabajo();
		orden.setId_orden(10L);
		orden.setEstado(EstadoOrden.LISTO);

		return orden;
	}

	@Test
	void noDejaEntregarSiLaOrdenNoSeCobro() {

		when(ordenRepository.findById(10L)).thenReturn(Optional.of(this.ordenLista()));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> ordenService.cambiarEstado(10L, EstadoOrden.ENTREGADO, 2L));

		verify(ordenRepository, never()).save(any());
		assertTrue(e.getMessage().contains("no se puede entregar"), e.getMessage());
	}

	@Test
	void noDejaEntregarSiElCobroEstaCargadoPeroSinConfirmar() {

		OrdenTrabajo orden = this.ordenLista();
		orden.setPagada(false);

		when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> ordenService.cambiarEstado(10L, EstadoOrden.ENTREGADO, 2L));

		verify(ordenRepository, never()).save(any());
		assertTrue(e.getMessage().contains("no se puede entregar"), e.getMessage());
	}

	@Test
	void dejaEntregarConElCobroConfirmado() {

		OrdenTrabajo orden = this.ordenLista();
		orden.setPagada(true);

		when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

		String resultado = ordenService.cambiarEstado(10L, EstadoOrden.ENTREGADO, 2L);

		verify(ordenRepository).save(orden);
		assertEquals(EstadoOrden.ENTREGADO, orden.getEstado());
		assertTrue(resultado.contains("paso a ENTREGADO"), resultado);
	}

	@Test
	void pagosServiceMarcaLaOrdenComoCobrada() {

		OrdenTrabajo orden = this.ordenLista();

		when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

		ordenService.marcarPagada(10L, true);

		verify(ordenRepository).save(orden);
		assertEquals(true, orden.getPagada());
	}

	@Test
	void losDemasEstadosNoDependenDelCobro() {

		OrdenTrabajo orden = this.ordenLista();
		orden.setEstado(EstadoOrden.RECIBIDO);

		when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

		ordenService.cambiarEstado(10L, EstadoOrden.EN_REPARACION, 2L);

		assertEquals(EstadoOrden.EN_REPARACION, orden.getEstado());
	}

	@Test
	void cadaCambioDeEstadoDejaSuMovimiento() {

		OrdenTrabajo orden = this.ordenLista();

		when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

		ordenService.cambiarEstado(10L, EstadoOrden.EN_REPARACION, 5L);

		verify(movimientoRepository).save(any());
	}

	@Test
	void reabrirUnaOrdenEntregadaLeSacaLaFechaDeEntrega() {

		OrdenTrabajo orden = this.ordenLista();
		orden.setEstado(EstadoOrden.ENTREGADO);
		orden.setFechaEntregaReal(java.time.LocalDateTime.now());

		when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

		ordenService.cambiarEstado(10L, EstadoOrden.EN_REPARACION, 2L);

		assertNull(orden.getFechaEntregaReal());
	}

	@Test
	void elTotalEsLaSumaDeLosItemsMenosElDescuento() {

		OrdenTrabajo orden = this.ordenLista();
		orden.setDescuento(new BigDecimal("5000"));

		ItemOrdenTrabajo mano = new ItemOrdenTrabajo();
		mano.setCantidad(2);
		mano.setPrecioUnitario(new BigDecimal("20000"));

		ItemOrdenTrabajo filtro = new ItemOrdenTrabajo();
		filtro.setCantidad(1);
		filtro.setPrecioUnitario(new BigDecimal("14000"));

		orden.setItems(List.of(mano, filtro));

		when(ordenRepository.findAll()).thenReturn(List.of(orden));

		OrdenTrabajo traida = ordenService.getOrdenes().get(0);

		assertEquals(new BigDecimal("54000"), traida.getSubtotal());
		assertEquals(new BigDecimal("49000"), traida.getTotal());
	}

}
