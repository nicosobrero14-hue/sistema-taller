package com.tallerapp.ventas_service.entity;

// Una venta no se borra: se anula. Asi el numero de comprobante no queda
// salteado y se puede explicar por que volvio el repuesto al deposito.
public enum EstadoVenta {

	HECHA,
	ANULADA

}
