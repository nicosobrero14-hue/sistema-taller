package com.tallerapp.ventas_service.entity;

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

// Una venta de mostrador: alguien entra, se lleva un repuesto y paga.
// No hay orden de trabajo ni vehiculo de por medio.
//
// Se cobra en el momento, asi que no tiene el ida y vuelta de PENDIENTE
// que si tienen los cobros de las ordenes: el repuesto no sale del
// mostrador hasta que la plata este.
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "ventas", indexes = {
		@Index(name = "idx_venta_fecha", columnList = "fecha"),
		@Index(name = "idx_venta_cliente", columnList = "id_cliente")
})
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_venta;

	// Numero de comprobante interno, correlativo. Sin valor fiscal, igual
	// que el recibo de las ordenes.
	private Long numeroComprobante;

	@Enumerated(EnumType.STRING)
	private EstadoVenta estado;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MedioVenta medio;

	// Quien compro. Puede ser un cliente del sistema (referencia a
	// clientes-service, sin relacion JPA) o nadie: el que pasa una vez y
	// paga en efectivo es consumidor final y no hace falta darlo de alta.
	private Long id_cliente;
	private String nombreCliente;

	// quien vendio y desde donde, referencias a usuarios-service
	private Long id_empleado;
	private Long id_sucursal;

	private LocalDateTime fecha;
	private String observaciones;

	// cuando se anula: cuando, quien y por que
	private LocalDateTime fechaAnulacion;
	private Long id_anulo;
	private String motivoAnulacion;

	// se suma de los detalles cada vez que se pide, no se guarda
	@Transient
	private BigDecimal total;

	@OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DetalleVenta> detalles;

}
