package com.tallerapp.taller_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.taller_service.entity.ItemOrdenTrabajo;

@Repository
public interface IItemOrdenTrabajoRepository extends JpaRepository<ItemOrdenTrabajo, Long> {
	
	@Query("SELECT i FROM ItemOrdenTrabajo i WHERE i.ordenTrabajo.id_orden = :id_orden")
	List<ItemOrdenTrabajo> findByOrdenId(@Param("id_orden") Long id_orden);

}
