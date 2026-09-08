package com.tallerapp.usuarios_service.service;

import java.util.List;

import com.tallerapp.usuarios_service.entity.Sucursal;



public interface ISucursalService {
	
	public void saveSucursal(String nombre, String direccion);
    List<Sucursal> getSucursal();
    public String deleteSucursal(Long id);
    public Sucursal findSucursal(Long id);
    public void editSucursal(Long id, Sucursal sucursal);

}
