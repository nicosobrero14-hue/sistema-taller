package com.tallerapp.inventario_service.service;

import java.util.List;

import com.tallerapp.inventario_service.entity.MovimientoInventario;
import com.tallerapp.inventario_service.entity.TipoMovimiento;

public interface IMovimientoInventarioService {
	
	public String saveMovimiento(Long id_repuesto, TipoMovimiento tipo, Integer cantidad, String motivo, Long id_orden, Long id_venta);
	public List<MovimientoInventario> getMovimientos();
	public MovimientoInventario findMovimiento(Long id);
	public List<MovimientoInventario> getMovimientosPorRepuesto(Long id_repuesto);

}
