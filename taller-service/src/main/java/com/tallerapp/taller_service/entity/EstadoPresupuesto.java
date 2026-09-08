package com.tallerapp.taller_service.entity;

public enum EstadoPresupuesto {
	
	VIGENTE,    // dentro de los dias de validez
	VENCIDO,    // se paso la fecha, hay que actualizar precios
	ACEPTADO,   // el cliente dijo que si y se genero la orden
	RECHAZADO   // el cliente no lo tomo

}
