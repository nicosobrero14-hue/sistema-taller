package com.tallerapp.ventas_service.entity;

// Los mismos medios que en los cobros de las ordenes. Estan repetidos a
// proposito: cada servicio es dueño de lo suyo y no comparte clases con
// los demas.
public enum MedioVenta {

	EFECTIVO,
	TRANSFERENCIA,
	TARJETA_DEBITO,
	TARJETA_CREDITO,
	QR

}
