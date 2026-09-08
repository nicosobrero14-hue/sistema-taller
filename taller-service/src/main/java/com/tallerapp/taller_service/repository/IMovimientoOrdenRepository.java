package com.tallerapp.taller_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.taller_service.entity.MovimientoOrden;

@Repository
public interface IMovimientoOrdenRepository extends JpaRepository<MovimientoOrden, Long> {

	// del mas viejo al mas nuevo: se lee como una linea de tiempo
	@Query("SELECT m FROM MovimientoOrden m WHERE m.ordenTrabajo.id_orden = :id_orden ORDER BY m.id_movimiento ASC")
	List<MovimientoOrden> buscarPorOrden(@Param("id_orden") Long id_orden);

}
