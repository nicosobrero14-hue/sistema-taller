package com.tallerapp.inventario_service.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
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

// cada entrada o salida de mercaderia queda registrada aca,
// asi se puede reconstruir por que el stock es el que es
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "movimientos_inventario", indexes = @Index(name = "idx_mov_repuesto", columnList = "repuesto_id"))
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_movimiento;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "repuesto_id", nullable = false)
	private Repuesto repuesto;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoMovimiento tipo;
	
	@Column(nullable = false)
	private Integer cantidad;
	
	// como quedo el stock despues de este movimiento
	private Integer stockResultante;
	
	private String motivo;
	private LocalDateTime fecha;
	
	// De donde salio el movimiento, si salio de algun lado. Son referencias
	// sin relacion JPA: la orden vive en taller-service y la venta en
	// ventas-service.
	private Long id_orden;
	private Long id_venta;

}
