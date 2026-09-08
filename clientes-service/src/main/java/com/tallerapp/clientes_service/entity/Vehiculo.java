package com.tallerapp.clientes_service.entity;

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

@Entity
@Table(name = "vehiculos", indexes = {
		@Index(name = "idx_vehiculo_patente", columnList = "patente"),
		@Index(name = "idx_vehiculo_cliente", columnList = "cliente_id")
})
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_vehiculo;
	
	@Column(nullable = false, unique = true)
	private String patente;
	private String marca;
	private String modelo;
	private Integer anio;
	private Integer kilometraje;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

}
