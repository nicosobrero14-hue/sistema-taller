package com.tallerapp.clientes_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tallerapp.clientes_service.entity.Cliente;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Long> {

}
