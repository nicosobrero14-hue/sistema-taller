package com.tallerapp.clientes_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.clientes_service.entity.Cliente;
import com.tallerapp.clientes_service.exception.RecursoNoEncontradoException;
import com.tallerapp.clientes_service.exception.ReglaNegocioException;
import com.tallerapp.clientes_service.repository.IClienteRepository;
import com.tallerapp.clientes_service.repository.IVehiculoRepository;

@Service
public class ClienteService implements IClienteService {
	
	@Autowired
	private IClienteRepository clienteRepository;
	
	@Autowired
	private IVehiculoRepository vehiculoRepository;
	
	
	@Override
	public void saveCliente(String nombre, String telefono, String email) {
		
		Cliente cliente = new Cliente();
		cliente.setNombre(nombre);
		cliente.setTelefono(telefono);
		cliente.setEmail(email);
		
		clienteRepository.save(cliente);
	}

	@Override
	public List<Cliente> getClientes() {
		
		return clienteRepository.findAll();
	}

	@Override
	public Cliente findCliente(Long id) {
		
		return clienteRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente con id " + id));
	}

	@Override
	public void editCliente(Long id, Cliente cliente) {
		
		Cliente cli = this.findCliente(id);
		cli.setNombre(cliente.getNombre());
		cli.setTelefono(cliente.getTelefono());
		cli.setEmail(cliente.getEmail());
		
		clienteRepository.save(cli);
		
	}

	@Override
	public String deleteCliente(Long id) {
		
		this.findCliente(id);
		
		// si tiene vehiculos cargados no se puede borrar, quedarian huerfanos
		int vehiculos = vehiculoRepository.findByClienteId(id).size();
		
		if (vehiculos > 0) {
			throw new ReglaNegocioException("No se puede borrar: el cliente tiene " + vehiculos + " vehiculo(s) cargado(s)");
		}
		
		clienteRepository.deleteById(id);
		
		return "El Cliente fue eliminado correctamente";
	}

}
