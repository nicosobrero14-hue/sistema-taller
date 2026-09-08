package com.tallerapp.ventas_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tallerapp.ventas_service.client.IInventarioClient;
import com.tallerapp.ventas_service.dto.DetalleVentaDTO;
import com.tallerapp.ventas_service.dto.MovimientoDTO;
import com.tallerapp.ventas_service.dto.RepuestoDTO;
import com.tallerapp.ventas_service.dto.VentaDTO;
import com.tallerapp.ventas_service.entity.DetalleVenta;
import com.tallerapp.ventas_service.entity.EstadoVenta;
import com.tallerapp.ventas_service.entity.MedioVenta;
import com.tallerapp.ventas_service.entity.Venta;
import com.tallerapp.ventas_service.exception.ReglaNegocioException;
import com.tallerapp.ventas_service.repository.IVentaRepository;

/**
 * Las reglas de la venta de mostrador: que no se venda lo que no hay y
 * que el precio lo ponga el deposito, no el navegador.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

	@Mock
	private IVentaRepository ventaRepository;

	@Mock
	private IInventarioClient inventarioClient;

	@InjectMocks
	private VentaService ventaService;


	private RepuestoDTO repuesto(Long id, String nombre, int stock, String precio) {

		RepuestoDTO repuesto = new RepuestoDTO();
		repuesto.setId_repuesto(id);
		repuesto.setNombre(nombre);
		repuesto.setStock(stock);
		repuesto.setPrecioVenta(new BigDecimal(precio));

		return repuesto;
	}

	private VentaDTO venta(Long id_repuesto, int cantidad) {

		DetalleVentaDTO detalle = new DetalleVentaDTO();
		detalle.setId_repuesto(id_repuesto);
		detalle.setCantidad(cantidad);

		VentaDTO venta = new VentaDTO();
		venta.setMedio(MedioVenta.EFECTIVO);
		venta.setId_sucursal(1L);
		venta.setDetalles(List.of(detalle));

		return venta;
	}


	@Test
	void noVendeLoQueNoHayEnElDeposito() {

		when(inventarioClient.traerRepuesto(1L)).thenReturn(this.repuesto(1L, "Filtro", 2, "14000"));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> ventaService.saveVenta(this.venta(1L, 5), 3L));

		verify(ventaRepository, never()).save(any());
		verify(inventarioClient, never()).moverStock(any());
		assertTrue(e.getMessage().contains("No hay stock"), e.getMessage());
	}

	@Test
	void siUnRenglonNoTieneStockNoSeDescuentaNinguno() {

		DetalleVentaDTO uno = new DetalleVentaDTO();
		uno.setId_repuesto(1L);
		uno.setCantidad(1);

		DetalleVentaDTO dos = new DetalleVentaDTO();
		dos.setId_repuesto(2L);
		dos.setCantidad(99);

		VentaDTO venta = new VentaDTO();
		venta.setMedio(MedioVenta.EFECTIVO);
		venta.setId_sucursal(1L);
		venta.setDetalles(List.of(uno, dos));

		when(inventarioClient.traerRepuesto(1L)).thenReturn(this.repuesto(1L, "Filtro", 10, "14000"));
		when(inventarioClient.traerRepuesto(2L)).thenReturn(this.repuesto(2L, "Pastillas", 3, "42000"));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> ventaService.saveVenta(venta, 3L));

		verify(inventarioClient, never()).moverStock(any());
		assertTrue(e.getMessage().contains("No hay stock de Pastillas"), e.getMessage());
	}

	@Test
	void elPrecioLoPoneElDepositoYSeCongelaEnLaVenta() {

		when(inventarioClient.traerRepuesto(1L)).thenReturn(this.repuesto(1L, "Filtro", 10, "14000"));

		String resultado = ventaService.saveVenta(this.venta(1L, 2), 3L);

		ArgumentCaptor<Venta> guardada = ArgumentCaptor.forClass(Venta.class);
		verify(ventaRepository).save(guardada.capture());

		DetalleVenta detalle = guardada.getValue().getDetalles().get(0);

		assertEquals(new BigDecimal("14000"), detalle.getPrecioUnitario());
		assertEquals(EstadoVenta.HECHA, guardada.getValue().getEstado());
		assertTrue(resultado.contains("28000"), resultado);
	}

	@Test
	void venderDescuentaDelDeposito() {

		when(inventarioClient.traerRepuesto(1L)).thenReturn(this.repuesto(1L, "Filtro", 10, "14000"));

		ventaService.saveVenta(this.venta(1L, 2), 3L);

		ArgumentCaptor<MovimientoDTO> movido = ArgumentCaptor.forClass(MovimientoDTO.class);
		verify(inventarioClient).moverStock(movido.capture());

		assertEquals("SALIDA", movido.getValue().getTipo());
		assertEquals(2, movido.getValue().getCantidad());
	}

	@Test
	void anularDevuelveLosRepuestosYPideMotivo() {

		DetalleVenta detalle = new DetalleVenta();
		detalle.setId_repuesto(1L);
		detalle.setCantidad(2);
		detalle.setDescripcion("Filtro");
		detalle.setPrecioUnitario(new BigDecimal("14000"));

		Venta venta = new Venta();
		venta.setId_venta(7L);
		venta.setNumeroComprobante(3L);
		venta.setEstado(EstadoVenta.HECHA);
		venta.setDetalles(List.of(detalle));

		when(ventaRepository.findById(7L)).thenReturn(Optional.of(venta));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> ventaService.anularVenta(7L, "   ", 3L));

		verify(inventarioClient, never()).moverStock(any());
		assertTrue(e.getMessage().contains("por que se anula"), e.getMessage());

		ventaService.anularVenta(7L, "El cliente lo devolvio", 3L);

		ArgumentCaptor<MovimientoDTO> movido = ArgumentCaptor.forClass(MovimientoDTO.class);
		verify(inventarioClient).moverStock(movido.capture());

		assertEquals("ENTRADA", movido.getValue().getTipo());
		assertEquals(EstadoVenta.ANULADA, venta.getEstado());
		assertEquals("El cliente lo devolvio", venta.getMotivoAnulacion());
	}

	@Test
	void anularDosVecesNoDevuelveElStockDeNuevo() {

		Venta venta = new Venta();
		venta.setNumeroComprobante(3L);
		venta.setEstado(EstadoVenta.ANULADA);

		when(ventaRepository.findById(7L)).thenReturn(Optional.of(venta));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> ventaService.anularVenta(7L, "otra vez", 3L));

		verify(inventarioClient, never()).moverStock(any());
		assertTrue(e.getMessage().contains("ya estaba anulada"), e.getMessage());
	}

	@Test
	void siNoSePudoDescontarNoQuedaLaVentaGuardada() {

		when(inventarioClient.traerRepuesto(1L)).thenReturn(this.repuesto(1L, "Filtro", 10, "14000"));
		when(inventarioClient.moverStock(any())).thenThrow(new RuntimeException("sin stock"));

		ReglaNegocioException e = assertThrows(ReglaNegocioException.class,
				() -> ventaService.saveVenta(this.venta(1L, 2), 3L));

		verify(ventaRepository, times(1)).delete(any());
		assertTrue(e.getMessage().contains("No se pudo mover el stock"), e.getMessage());
	}

}
