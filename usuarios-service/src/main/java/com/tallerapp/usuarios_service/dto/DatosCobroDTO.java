package com.tallerapp.usuarios_service.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lo que necesita el que esta atendiendo el mostrador: el encabezado del
 * comprobante y adonde le tiene que decir al cliente que pague.
 *
 * Va aparte de la configuracion entera a proposito. Un vendedor tiene que
 * poder imprimir un recibo y dictar el CBU sin ver ni tocar la condicion
 * fiscal, el punto de venta ni el precio de la hora.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DatosCobroDTO {

	private String razonSocial;
	private String nombreFantasia;
	private String cuit;
	private String domicilio;
	private String telefono;
	private BigDecimal porcentajeIva;

	private String banco;
	private String cbu;
	private String aliasCbu;
	private String titularCuenta;
	private String datosTarjeta;
	private String datosQr;

}
