package com.tallerapp.usuarios_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Los datos del taller que van en los comprobantes. Es una sola fila:
// no tiene sentido tener dos configuraciones del mismo taller.
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "configuracion_taller")
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionTaller {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_configuracion;
	
	private String razonSocial;
	private String nombreFantasia;
	private String cuit;
	private String domicilio;
	private String telefono;
	private String email;
	
	// MONOTRIBUTO o RESPONSABLE_INSCRIPTO: define que tipo de comprobante
	// se puede emitir el dia que se conecte con ARCA
	private String condicionFiscal;
	
	// el punto de venta habilitado para facturar
	private String puntoVenta;
	
	// cuanto se cobra la hora de mano de obra, para calcular presupuestos
	private java.math.BigDecimal precioHoraManoObra;
	
	// El IVA con el que se discriminan los comprobantes. Los precios que se
	// cargan ya lo llevan adentro: esto no suma nada al total, solo dice
	// cuanto de ese total es impuesto.
	private java.math.BigDecimal porcentajeIva;
	
	// ---------- adonde tiene que pagar el cliente ----------
	// Esto se le muestra al cliente en el mostrador, asi que leerlo no es
	// secreto. Lo que si es delicado es cambiarlo: el que cambia el CBU
	// desvia la plata del taller. Por eso editar sigue siendo solo del
	// administrador.
	private String banco;
	private String cbu;
	private String aliasCbu;
	private String titularCuenta;
	
	// texto libre, cada taller trabaja distinto ("Posnet Mercado Pago,
	// hasta 3 cuotas sin interes", "QR de Mercado Pago en el mostrador")
	private String datosTarjeta;
	private String datosQr;

}
