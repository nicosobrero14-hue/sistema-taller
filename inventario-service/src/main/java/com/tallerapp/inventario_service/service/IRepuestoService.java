package com.tallerapp.inventario_service.service;

import java.math.BigDecimal;
import java.util.List;

import com.tallerapp.inventario_service.entity.PrecioRepuesto;
import com.tallerapp.inventario_service.entity.Repuesto;

public interface IRepuestoService {
	
	public String saveRepuesto(String codigo, String codigoBarra, String nombre, String descripcion,
							   String marca, String ubicacion, BigDecimal precioCompra, BigDecimal precioVenta,
							   Integer stock, Integer stockMinimo, Long id_sucursal);
	public List<Repuesto> getRepuestos();
	public String deleteRepuesto(Long id);
	public Repuesto findRepuesto(Long id);
	public String editRepuesto(Long id, Repuesto repuesto, Long id_empleado);
	// como fue cambiando el precio de este repuesto
	public List<PrecioRepuesto> getHistorialPrecios(Long id_repuesto);
	
	// el que usa la pistola: codigo exacto
	public Repuesto findPorCodigoBarra(String codigoBarra);
	// el buscador del deposito: texto parcial
	public List<Repuesto> buscar(String texto);
	// los que hay que reponer
	public List<Repuesto> getBajoStock();

}
