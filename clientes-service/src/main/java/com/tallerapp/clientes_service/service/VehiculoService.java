package com.tallerapp.clientes_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.clientes_service.client.ITallerClient;
import com.tallerapp.clientes_service.entity.Cliente;
import com.tallerapp.clientes_service.entity.Vehiculo;
import com.tallerapp.clientes_service.exception.RecursoNoEncontradoException;
import com.tallerapp.clientes_service.exception.ReglaNegocioException;
import com.tallerapp.clientes_service.repository.IClienteRepository;
import com.tallerapp.clientes_service.repository.IVehiculoRepository;

@Service
public class VehiculoService implements IVehiculoService {
	
	@Autowired
	private IVehiculoRepository vehiculoRepository;
	
	@Autowired
	private IClienteRepository clienteRepository;
	
	@Autowired
	private ITallerClient tallerClient;
	
	
	@Override
	public void saveVehiculo(String patente, String marca, String modelo, Integer anio, Integer kilometraje, Long id_cliente) {
		
		Cliente cliente = clienteRepository.findById(id_cliente)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con id " + id_cliente));
		
		Vehiculo vehiculo = new Vehiculo();
		vehiculo.setPatente(patente);
		vehiculo.setMarca(marca);
		vehiculo.setModelo(modelo);
		vehiculo.setAnio(anio);
		vehiculo.setKilometraje(kilometraje);
		vehiculo.setCliente(cliente);
		
		vehiculoRepository.save(vehiculo);
	}

	@Override
	public List<Vehiculo> getVehiculos() {
		
		return vehiculoRepository.findAll();
	}

	@Override
	public Vehiculo findVehiculo(Long id) {
		
		return vehiculoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el vehiculo con id " + id));
	}

	@Override
	public void editVehiculo(Long id, Vehiculo vehiculo) {
		
		Vehiculo vehi = this.findVehiculo(id);
		vehi.setPatente(vehiculo.getPatente());
		vehi.setMarca(vehiculo.getMarca());
		vehi.setModelo(vehiculo.getModelo());
		vehi.setAnio(vehiculo.getAnio());
		vehi.setKilometraje(vehiculo.getKilometraje());
		
		// el cliente llega solo con el id, hay que buscarlo en la base
		if (vehiculo.getCliente() != null) {
			
			Long id_cliente = vehiculo.getCliente().getId_cliente();
			
			vehi.setCliente(clienteRepository.findById(id_cliente)
					.orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con id " + id_cliente)));
		}
		
		vehiculoRepository.save(vehi);
		
	}

	@Override
	public String deleteVehiculo(Long id) {
		
		this.findVehiculo(id);
		
		// las ordenes viven en taller-service y guardan el id_vehiculo suelto,
		// sin foreign key. Si borramos el vehiculo igual, esas ordenes quedan
		// apuntando a algo que ya no existe, asi que preguntamos primero.
		Long ordenes = this.contarOrdenes(id);
		
		if (ordenes == null) {
			throw new ReglaNegocioException("No se puede borrar: taller-service no responde y no se puede verificar el historial");
		}
		
		if (ordenes > 0) {
			throw new ReglaNegocioException("No se puede borrar: el vehiculo tiene " + ordenes + " orden(es) de trabajo en su historial");
		}
		
		vehiculoRepository.deleteById(id);
		
		return "El Vehiculo fue eliminado correctamente";
	}
	
	// devuelve null si taller-service no esta levantado
	private Long contarOrdenes(Long id_vehiculo) {
		
		try {
			return tallerClient.contarOrdenes(id_vehiculo);
		} catch (Exception e) {
			return null;
		}
	}

}
