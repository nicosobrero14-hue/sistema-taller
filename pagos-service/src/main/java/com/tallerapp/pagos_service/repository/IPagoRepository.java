package com.tallerapp.pagos_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.pagos_service.entity.Pago;

@Repository
public interface IPagoRepository extends JpaRepository<Pago, Long> {
	
	// Va con @Query porque el campo se llama id_orden: Spring Data no puede
	// derivarlo del nombre del metodo. Devuelve todos los cobros de la orden
	// (puede haber anulados) del mas nuevo al mas viejo; cual es el que vale
	// lo decide el service.
	@Query("SELECT p FROM Pago p WHERE p.id_orden = :id_orden ORDER BY p.id_pago DESC")
	List<Pago> buscarPorOrden(@Param("id_orden") Long id_orden);
	
	@Query("SELECT COALESCE(MAX(p.numeroRecibo), 0) FROM Pago p")
	Long ultimoNumeroRecibo();

}
