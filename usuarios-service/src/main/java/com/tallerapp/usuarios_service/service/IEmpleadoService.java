package com.tallerapp.usuarios_service.service;

import java.util.List;

import com.tallerapp.usuarios_service.entity.Empleado;
import com.tallerapp.usuarios_service.entity.Rol;

public interface IEmpleadoService {
	
	public String saveEmpleado(String nombre, String apellido, String email, String password, Rol rol, Long id_sucursal);
	public List<Empleado> getEmpleados();
	public String deleteEmpleado(Long id);
	public void editEmpleado(Long id, Empleado empleado);
	public String bloquearEmpleado(Long id, boolean activo, Long id_quienPide);
	public Empleado findEmpleado(Long id);
	
	
}
