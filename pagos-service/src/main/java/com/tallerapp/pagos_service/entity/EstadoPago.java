package com.tallerapp.pagos_service.entity;

// El cobro no queda hecho apenas se carga. Primero se registra como
// PENDIENTE (el cliente dice que transfirio, paso la tarjeta, etc) y
// despues alguien mira la cuenta y confirma que la plata entro de verdad.
// El recibo se emite recien en ese momento.
//
// ANULADO es el unico final posible de un cobro que se cae. No se borra:
// un recibo que ya se imprimio y quedo en la mano del cliente no puede
// desaparecer del sistema, se anula y queda a la vista con su motivo.
public enum EstadoPago {

	PENDIENTE,
	CONFIRMADO,
	ANULADO

}
