package com.tallerapp.inventario_service.entity;

public enum TipoMovimiento {
	
	ENTRADA,   // compra a proveedor, devolucion
	SALIDA,    // se uso en una orden de trabajo o se vendio
	AJUSTE     // correccion despues de contar el deposito

}
