package com.tallerapp.clientes_service.service;

import java.util.List;

import com.tallerapp.clientes_service.entity.Cliente;

public interface IClienteService {
	
	public void saveCliente(String nombre, String telefono, String email);
	public List<Cliente> getClientes();
	public String deleteCliente(Long id);
	public Cliente findCliente(Long id);
	public void editCliente(Long id, Cliente cliente);

}
