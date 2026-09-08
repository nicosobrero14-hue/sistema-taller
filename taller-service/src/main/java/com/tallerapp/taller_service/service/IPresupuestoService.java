package com.tallerapp.taller_service.service;

import java.math.BigDecimal;
import java.util.List;

import com.tallerapp.taller_service.dto.ItemPresupuestoDTO;
import com.tallerapp.taller_service.entity.Presupuesto;

public interface IPresupuestoService {
	
	public String savePresupuesto(Long id_vehiculo, Long id_empleado, Long id_sucursal, String detalleTrabajo,
								  Integer validezDias, BigDecimal horasEstimadas, BigDecimal precioHora,
								  List<ItemPresupuestoDTO> items);
	public List<Presupuesto> getPresupuestos();
	public Presupuesto findPresupuesto(Long id);
	public List<Presupuesto> getPresupuestosPorVehiculo(Long id_vehiculo);
	public String elegirOpcion(Long id_presupuesto, Integer opcion);
	public String aceptarPresupuesto(Long id, Long id_mecanico, Long id_empleado);
	public String rechazarPresupuesto(Long id);
	public String renovarPresupuesto(Long id);
	public String deletePresupuesto(Long id);

}
