package com.tallerapp.taller_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.taller_service.entity.FotoOrden;

@Repository
public interface IFotoOrdenRepository extends JpaRepository<FotoOrden, Long> {
	
	@Query("SELECT f FROM FotoOrden f WHERE f.ordenTrabajo.id_orden = :id_orden ORDER BY f.fecha")
	List<FotoOrden> findByOrdenId(@Param("id_orden") Long id_orden);

}
