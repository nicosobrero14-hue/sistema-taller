package com.tallerapp.taller_service.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cada vez que la orden cambia de estado queda una linea aca. Sirve para
// contestar "quien paso esta orden a ENTREGADO y cuando", que hasta ahora
// no tenia respuesta.
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "movimientos_orden", indexes = {
		@Index(name = "idx_movimiento_orden", columnList = "orden_id")
})
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoOrden {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_movimiento;

	// misma base, asi que aca si va la relacion de verdad
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orden_id", nullable = false)
	@JsonIgnore
	private OrdenTrabajo ordenTrabajo;

	@Enumerated(EnumType.STRING)
	private EstadoOrden estadoAnterior;

	@Enumerated(EnumType.STRING)
	private EstadoOrden estadoNuevo;

	private LocalDateTime fecha;

	// quien lo hizo, referencia a usuarios-service
	private Long id_empleado;

	// para dejar dicho algo mas que el cambio de estado (ej: "orden creada")
	private String detalle;

}
