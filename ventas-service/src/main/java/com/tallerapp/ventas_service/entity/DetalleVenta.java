package com.tallerapp.ventas_service.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

// Cada renglon de la venta. La descripcion y el precio se copian del
// deposito en el momento de vender: si mañana cambia el precio del
// repuesto, esta venta sigue diciendo lo que se cobro.
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "detalles_venta", indexes = @Index(name = "idx_detalle_venta", columnList = "venta_id"))
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_detalle;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "venta_id", nullable = false)
	@JsonIgnore
	private Venta venta;

	// referencia a inventario-service, sin relacion JPA
	@Column(nullable = false)
	private Long id_repuesto;

	private String descripcion;

	@Column(nullable = false)
	private Integer cantidad;

	@Column(nullable = false)
	private BigDecimal precioUnitario;

}
