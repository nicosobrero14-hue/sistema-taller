package com.tallerapp.taller_service.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

@Entity
@Table(name = "items_orden_trabajo", indexes = @Index(name = "idx_item_orden", columnList = "orden_id"))
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrdenTrabajo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_item;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orden_id", nullable = false)
	private OrdenTrabajo ordenTrabajo;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoItem tipo;
	
	// reservado para la Fase 2 (inventario-service), por ahora queda vacio
	private Long id_repuesto;
	
	@Column(nullable = false)
	private String descripcion;
	private Integer cantidad;
	private BigDecimal precioUnitario;

}
