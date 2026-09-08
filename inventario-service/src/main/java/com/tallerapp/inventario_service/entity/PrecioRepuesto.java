package com.tallerapp.inventario_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

/**
 * Cada vez que cambia el precio de un repuesto queda una fila aca.
 *
 * Sin esto, al actualizar el precio se pierde el anterior: no se puede
 * saber a cuanto se vendia hace tres meses ni justificarle un aumento
 * al cliente que vuelve.
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "precios_repuesto", indexes = @Index(name = "idx_precio_repuesto", columnList = "repuesto_id"))
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class PrecioRepuesto {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_precio;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "repuesto_id", nullable = false)
	private Repuesto repuesto;
	
	private BigDecimal precioCompra;
	private BigDecimal precioVenta;
	
	// desde cuando rige este precio
	private LocalDateTime fecha;
	
	// quien lo cambio, referencia a usuarios-service
	private Long id_empleado;

}
