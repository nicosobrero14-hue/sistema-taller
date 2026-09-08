package com.tallerapp.pagos_service.service;

import java.math.BigDecimal;
import java.util.List;

import com.tallerapp.pagos_service.entity.MedioPago;
import com.tallerapp.pagos_service.entity.Pago;

public interface IPagoService {
	
	public String savePago(Long id_orden, BigDecimal monto, MedioPago medio, String observaciones, Long id_empleado);
	public List<Pago> getPagos();
	public Pago findPago(Long id);
	public Pago findPagoPorOrden(Long id_orden);
	public List<Pago> getPagosDeOrden(Long id_orden);
	public String confirmarPago(Long id, Long id_empleado);
	public String anularPago(Long id, String motivo, Long id_empleado);

}
