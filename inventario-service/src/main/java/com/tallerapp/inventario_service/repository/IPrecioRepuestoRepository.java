package com.tallerapp.inventario_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.inventario_service.entity.PrecioRepuesto;

@Repository
public interface IPrecioRepuestoRepository extends JpaRepository<PrecioRepuesto, Long> {
	
	@Query("SELECT p FROM PrecioRepuesto p WHERE p.repuesto.id_repuesto = :id_repuesto ORDER BY p.fecha DESC")
	List<PrecioRepuesto> findByRepuestoId(@Param("id_repuesto") Long id_repuesto);

}
