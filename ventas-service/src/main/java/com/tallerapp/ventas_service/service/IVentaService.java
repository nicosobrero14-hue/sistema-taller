package com.tallerapp.ventas_service.service;

import java.util.List;

import com.tallerapp.ventas_service.dto.VentaDTO;
import com.tallerapp.ventas_service.entity.Venta;

public interface IVentaService {

	public String saveVenta(VentaDTO ventaDTO, Long id_empleado);
	public String anularVenta(Long id, String motivo, Long id_empleado);
	public List<Venta> getVentas();
	public Venta findVenta(Long id);

}
