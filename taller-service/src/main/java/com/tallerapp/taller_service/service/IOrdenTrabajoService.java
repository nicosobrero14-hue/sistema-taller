package com.tallerapp.taller_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tallerapp.taller_service.entity.EstadoOrden;
import com.tallerapp.taller_service.entity.MovimientoOrden;
import com.tallerapp.taller_service.entity.OrdenTrabajo;

public interface IOrdenTrabajoService {
	
	public String saveOrden(Long id_vehiculo, Long id_mecanico, Long id_sucursal, String diagnostico, LocalDateTime fechaEntregaEstimada, Integer kilometrajeIngreso, Long id_empleado);
	public List<OrdenTrabajo> getOrdenes();
	public List<OrdenTrabajo> getAbiertas();
	public List<OrdenTrabajo> getEntregadas(LocalDate desde, LocalDate hasta);
	public String deleteOrden(Long id);
	public OrdenTrabajo findOrden(Long id);
	public String editOrden(Long id, OrdenTrabajo orden);
	public String cambiarEstado(Long id, EstadoOrden estado, Long id_empleado);
	public List<MovimientoOrden> getMovimientos(Long id_orden);
	public void marcarPagada(Long id, boolean pagada);
	public List<OrdenTrabajo> getOrdenesPorVehiculo(Long id_vehiculo);
	public Long contarPorVehiculo(Long id_vehiculo);
	public Long contarPorMecanico(Long id_mecanico);

}
