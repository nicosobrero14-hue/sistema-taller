package com.tallerapp.ventas_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tallerapp.ventas_service.entity.Venta;

@Repository
public interface IVentaRepository extends JpaRepository<Venta, Long> {

	@Query("SELECT COALESCE(MAX(v.numeroComprobante), 0) FROM Venta v")
	Long ultimoNumeroComprobante();

	// de la mas nueva a la mas vieja: es como se mira una lista de ventas
	@Query("SELECT v FROM Venta v ORDER BY v.id_venta DESC")
	List<Venta> traerOrdenadas();

}
