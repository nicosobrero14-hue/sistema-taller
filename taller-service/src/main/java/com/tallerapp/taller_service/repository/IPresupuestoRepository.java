package com.tallerapp.taller_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.taller_service.entity.Presupuesto;

@Repository
public interface IPresupuestoRepository extends JpaRepository<Presupuesto, Long> {
	
	@Query("SELECT p FROM Presupuesto p WHERE p.id_vehiculo = :id_vehiculo ORDER BY p.fechaEmision DESC")
	List<Presupuesto> findByVehiculoId(@Param("id_vehiculo") Long id_vehiculo);

}
