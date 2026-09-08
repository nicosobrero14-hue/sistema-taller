package com.tallerapp.pagos_service.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// El cobro de una orden. Se cobra todo junto al entregar el vehiculo:
// si el cliente no sabe cuanto va a salir, primero pide un presupuesto.
// Se carga en dos pasos: primero se registra y despues se confirma que
// la plata impacto de verdad.
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "pagos", indexes = {
		@Index(name = "idx_pago_orden", columnList = "id_orden")
})
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class Pago {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_pago;
	
	// Referencia a taller-service, sin relacion JPA. No es unique: una orden
	// puede tener un cobro anulado y otro nuevo hecho como corresponde.
	@Column(nullable = false)
	private Long id_orden;
	
	@Column(nullable = false)
	private BigDecimal monto;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MedioPago medio;
	
	// PENDIENTE mientras nadie corroboro que la plata entro
	@Enumerated(EnumType.STRING)
	private EstadoPago estado;
	
	// cuando se cargo el cobro y quien lo cargo (usuarios-service)
	private LocalDateTime fecha;
	private String observaciones;
	private Long id_empleado;
	
	// cuando se confirmo que la plata entro y quien lo confirmo
	private LocalDateTime fechaConfirmacion;
	private Long id_confirmo;
	
	// por que se anulo, cuando y quien. El motivo es obligatorio: un cobro
	// que se cae sin explicacion es justamente lo que hay que evitar.
	private LocalDateTime fechaAnulacion;
	private Long id_anulo;
	private String motivoAnulacion;
	
	// Numero del recibo interno. Es correlativo y arranca en 1. Se emite
	// al confirmar, no al cargar: si no, un cobro que despues se anula
	// deja un numero salteado en el medio.
	// No tiene valor fiscal: el dia que se conecte con ARCA, la factura
	// va a llevar su propia numeracion por punto de venta.
	private Long numeroRecibo;

}
