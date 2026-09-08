package com.tallerapp.inventario_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.inventario_service.entity.Repuesto;

@Repository
public interface IRepuestoRepository extends JpaRepository<Repuesto, Long> {
	
	// lo que usa la pistola de codigo de barras
	Optional<Repuesto> findByCodigoBarra(String codigoBarra);
	
	// el codigo interno, para cargar a mano
	Optional<Repuesto> findByCodigo(String codigo);
	
	// buscador del deposito: escribis cualquier cosa y busca por los dos
	// codigos, el nombre y la marca
	@Query("SELECT r FROM Repuesto r WHERE "
		 + "LOWER(r.codigo) LIKE LOWER(CONCAT('%', :texto, '%')) OR "
		 + "LOWER(r.codigoBarra) LIKE LOWER(CONCAT('%', :texto, '%')) OR "
		 + "LOWER(r.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR "
		 + "LOWER(r.marca) LIKE LOWER(CONCAT('%', :texto, '%'))")
	List<Repuesto> buscar(@Param("texto") String texto);
	
	// los que hay que reponer
	@Query("SELECT r FROM Repuesto r WHERE r.stock <= r.stockMinimo")
	List<Repuesto> findBajoStock();

}
