package com.tallerapp.clientes_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.clientes_service.entity.Vehiculo;

@Repository
public interface IVehiculoRepository extends JpaRepository<Vehiculo, Long> {
	
	@Query("SELECT v FROM Vehiculo v WHERE v.cliente.id_cliente = :id_cliente")
	List<Vehiculo> findByClienteId(@Param("id_cliente") Long id_cliente);

}
