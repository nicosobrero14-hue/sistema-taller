package com.tallerapp.inventario_service.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "repuestos", indexes = {
		@Index(name = "idx_repuesto_codigo", columnList = "codigo"),
		@Index(name = "idx_repuesto_barra", columnList = "codigoBarra"),
		@Index(name = "idx_repuesto_nombre", columnList = "nombre")
})
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class Repuesto {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_repuesto;
	
	// codigo interno del taller, el que se usa a mano
	@Column(nullable = false, unique = true)
	private String codigo;
	
	// el que trae impreso el envase. Puede estar vacio: no todos los
	// repuestos vienen con codigo de barras.
	@Column(unique = true)
	private String codigoBarra;
	
	@Column(nullable = false)
	private String nombre;
	private String descripcion;
	private String marca;
	
	// donde esta guardado en el deposito (estante, cajon)
	private String ubicacion;
	
	private BigDecimal precioCompra;
	private BigDecimal precioVenta;
	
	private Integer stock;
	
	// cuando el stock llega a este numero hay que reponer
	private Integer stockMinimo;
	
	// referencia a usuarios-service, sin relacion JPA
	private Long id_sucursal;

}
