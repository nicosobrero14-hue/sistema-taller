package com.tallerapp.clientes_service.service;

import java.util.List;

import com.tallerapp.clientes_service.entity.Vehiculo;

public interface IVehiculoService {
	
	public void saveVehiculo(String patente, String marca, String modelo, Integer anio, Integer kilometraje, Long id_cliente);
	public List<Vehiculo> getVehiculos();
	public String deleteVehiculo(Long id);
	public Vehiculo findVehiculo(Long id);
	public void editVehiculo(Long id, Vehiculo vehiculo);

}
