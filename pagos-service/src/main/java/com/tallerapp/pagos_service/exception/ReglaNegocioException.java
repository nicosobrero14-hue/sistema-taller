package com.tallerapp.pagos_service.exception;

// Lo que el sistema no deja hacer: el mail repetido, el monto que no
// coincide, la orden que no se puede entregar. Sale como 400 para que el
// que llama sepa que fallo, y no como un 200 con un texto adentro.
public class ReglaNegocioException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ReglaNegocioException(String mensaje) {
		super(mensaje);
	}

}
