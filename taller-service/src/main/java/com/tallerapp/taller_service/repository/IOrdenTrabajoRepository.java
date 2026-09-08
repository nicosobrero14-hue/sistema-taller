package com.tallerapp.taller_service.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerapp.taller_service.entity.OrdenTrabajo;

@Repository
public interface IOrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Long> {
	
	@Query("SELECT o FROM OrdenTrabajo o WHERE o.id_vehiculo = :id_vehiculo")
	List<OrdenTrabajo> findByVehiculoId(@Param("id_vehiculo") Long id_vehiculo);

	// Las que todavia estan en el taller. Es la pantalla del dia a dia, y
	// no crece: lo que se entrega sale de aca.
	@Query("SELECT o FROM OrdenTrabajo o WHERE o.estado <> com.tallerapp.taller_service.entity.EstadoOrden.ENTREGADO ORDER BY o.id_orden DESC")
	List<OrdenTrabajo> buscarAbiertas();

	// El archivo: las entregadas en un rango de fechas, de la mas nueva a
	// la mas vieja. Con miles de ordenes traerlas todas no sirve, asi que
	// siempre se pide un pedazo.
	@Query("SELECT o FROM OrdenTrabajo o WHERE o.estado = com.tallerapp.taller_service.entity.EstadoOrden.ENTREGADO "
		 + "AND o.fechaEntregaReal >= :desde AND o.fechaEntregaReal < :hasta ORDER BY o.fechaEntregaReal DESC")
	List<OrdenTrabajo> buscarEntregadas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
	
	// los usan clientes-service y usuarios-service antes de borrar,
	// para no dejar ordenes apuntando a algo que ya no existe
	@Query("SELECT COUNT(o) FROM OrdenTrabajo o WHERE o.id_vehiculo = :id_vehiculo")
	Long contarPorVehiculo(@Param("id_vehiculo") Long id_vehiculo);
	
	@Query("SELECT COUNT(o) FROM OrdenTrabajo o WHERE o.id_mecanico = :id_mecanico")
	Long contarPorMecanico(@Param("id_mecanico") Long id_mecanico);

}
