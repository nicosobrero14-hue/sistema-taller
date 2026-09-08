package com.tallerapp.inventario_service.exception;

public class StockInsuficienteException extends RuntimeException {
	
	public StockInsuficienteException(String mensaje) {
		super(mensaje);
	}

}
