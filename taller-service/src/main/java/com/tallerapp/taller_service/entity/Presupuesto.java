package com.tallerapp.taller_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "presupuestos", indexes = {
		@Index(name = "idx_presu_vehiculo", columnList = "id_vehiculo"),
		@Index(name = "idx_presu_estado", columnList = "estado")
})
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class Presupuesto {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_presupuesto;
	
	@Column(nullable = false)
	private Long id_vehiculo;
	private Long id_empleado;
	private Long id_sucursal;
	
	// observaciones largas: que hay que hacer, que se detecto, aclaraciones
	@Column(columnDefinition = "TEXT")
	private String detalleTrabajo;
	
	private LocalDateTime fechaEmision;
	
	// cuantos dias vale el precio. Pasado eso hay que actualizarlo.
	private Integer validezDias;
	private LocalDateTime fechaVencimiento;
	
	@Enumerated(EnumType.STRING)
	private EstadoPresupuesto estado;
	
	// Mano de obra estimada. Es una estimacion: el trabajo real puede
	// llevar mas horas, y esas se cargan despues en la orden.
	private BigDecimal horasEstimadas;
	private BigDecimal precioHora;
	
	// si el cliente lo acepto, la orden que salio de este presupuesto
	private Long id_orden;
	
	@OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ItemPresupuesto> items;
	
	// calculados al vuelo, no se guardan
	@Transient
	private BigDecimal totalRepuestos;
	
	@Transient
	private BigDecimal totalManoObra;
	
	@Transient
	private BigDecimal total;
	
	@Transient
	private Boolean vencido;
	
	// cual de las opciones eligio el cliente
	private Integer opcionElegida;
	
	// Cuanto sale cada opcion, con los repuestos que van siempre y la mano
	// de obra ya sumados: {1: 226000, 2: 282000}. Se calcula al leer.
	@Transient
	private java.util.Map<Integer, BigDecimal> totalesOpciones;

}
