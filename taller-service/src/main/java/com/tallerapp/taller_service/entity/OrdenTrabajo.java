package com.tallerapp.taller_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "ordenes_trabajo", indexes = {
		@Index(name = "idx_orden_vehiculo", columnList = "id_vehiculo"),
		@Index(name = "idx_orden_mecanico", columnList = "id_mecanico"),
		@Index(name = "idx_orden_estado", columnList = "estado")
})
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajo {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_orden;
	
	// referencia a clientes-service, sin relacion JPA
	@Column(nullable = false)
	private Long id_vehiculo;
	
	// referencias a usuarios-service, sin relacion JPA
	@Column(nullable = false)
	private Long id_mecanico;
	
	@Column(nullable = false)
	private Long id_sucursal;
	
	@Enumerated(EnumType.STRING)
	private EstadoOrden estado;
	private String diagnostico;

	// Con cuantos kilometros entro el vehiculo. Queda guardado en la orden
	// (y no solo en el vehiculo, que se pisa) para poder ver cuanto anduvo
	// entre un service y el siguiente.
	private Integer kilometrajeIngreso;

	private LocalDateTime fechaIngreso;
	private LocalDateTime fechaEntregaEstimada;
	
	// se completa sola cuando la orden pasa a ENTREGADO
	private LocalDateTime fechaEntregaReal;

	// La pone pagos-service al confirmar o anular el cobro. Se guarda aca
	// para no tener que preguntarle a pagos-service en cada cambio de estado.
	private Boolean pagada;
	
	// Descuento en pesos sobre el total. Va en plata y no en porcentaje
	// para que no haya discusiones de redondeo: lo que dice es lo que se
	// resta.
	private BigDecimal descuento;
	
	// @Transient = no existen como columna. Se calculan a partir de los items
	// cada vez que se pide la orden, asi nunca quedan desactualizados.
	// subtotal = suma de los items · total = subtotal - descuento
	@Transient
	private BigDecimal subtotal;
	
	@Transient
	private BigDecimal total;
	
	@OneToMany(mappedBy = "ordenTrabajo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ItemOrdenTrabajo> items;
	
	// la linea de tiempo de la orden: quien la movio de estado y cuando
	@OneToMany(mappedBy = "ordenTrabajo", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<MovimientoOrden> movimientos;

}
