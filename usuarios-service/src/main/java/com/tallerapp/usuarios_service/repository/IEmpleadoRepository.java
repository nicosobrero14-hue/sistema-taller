package com.tallerapp.usuarios_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.usuarios_service.entity.Empleado;

@Repository
public interface IEmpleadoRepository extends JpaRepository<Empleado, Long> {
	
	// el login entra por el email
	Optional<Empleado> findByEmail(String email);
	
	@Query("SELECT e FROM Empleado e WHERE e.sucursal.id_sucursal = :id_sucursal")
	List<Empleado> findBySucursalId(@Param("id_sucursal") Long id_sucursal);

}
