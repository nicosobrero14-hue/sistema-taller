package com.tallerapp.inventario_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.inventario_service.entity.MovimientoInventario;

@Repository
public interface IMovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
	
	@Query("SELECT m FROM MovimientoInventario m WHERE m.repuesto.id_repuesto = :id_repuesto ORDER BY m.fecha DESC")
	List<MovimientoInventario> findByRepuestoId(@Param("id_repuesto") Long id_repuesto);

}
