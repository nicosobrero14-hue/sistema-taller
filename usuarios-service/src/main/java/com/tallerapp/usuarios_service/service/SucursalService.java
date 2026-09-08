package com.tallerapp.usuarios_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.usuarios_service.entity.Sucursal;
import com.tallerapp.usuarios_service.exception.RecursoNoEncontradoException;
import com.tallerapp.usuarios_service.exception.ReglaNegocioException;
import com.tallerapp.usuarios_service.repository.IEmpleadoRepository;
import com.tallerapp.usuarios_service.repository.ISucursalRepository;

@Service
public class SucursalService implements ISucursalService {

	@Autowired
	private ISucursalRepository sucursalRepository;
	
	@Autowired
	private IEmpleadoRepository empleadoRepository;
	
	
	@Override
	public void saveSucursal(String nombre, String direccion) {
		
		Sucursal sucursal = new Sucursal();
		sucursal.setNombre(nombre);
		sucursal.setDireccion(direccion);
		
		sucursalRepository.save(sucursal);
	}

	@Override
	public List<Sucursal> getSucursal() {
		
		return sucursalRepository.findAll();
	}

	@Override
	public Sucursal findSucursal(Long id) {
		
		return sucursalRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la sucursal con id " + id));	
	}

	@Override
	public void editSucursal(Long id, Sucursal sucursal) {
		
		Sucursal sucu = this.findSucursal(id);
		sucu.setNombre(sucursal.getNombre());
		sucu.setDireccion(sucursal.getDireccion());
		
		sucursalRepository.save(sucu);
		
	}

	@Override
	public String deleteSucursal(Long id) {

		this.findSucursal(id);
		
		// si tiene empleados asignados no se puede borrar
		int empleados = empleadoRepository.findBySucursalId(id).size();
		
		if (empleados > 0) {
			throw new ReglaNegocioException("No se puede borrar: la sucursal tiene " + empleados + " empleado(s) asignado(s)");
		}
		
		sucursalRepository.deleteById(id);
		
		return "La Sucursal fue eliminada correctamente";
	}


	

}
