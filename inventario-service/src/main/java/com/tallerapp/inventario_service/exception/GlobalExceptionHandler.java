package com.tallerapp.inventario_service.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	//1- cuando se pide un id que no existe -> 404 con el mensaje
	@ExceptionHandler(RecursoNoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String recursoNoEncontrado(RecursoNoEncontradoException e) {
		return e.getMessage();
	}
	
	//2- cuando se quiere sacar mas mercaderia de la que hay -> 409.
	// Va con un codigo de error propio para que taller-service pueda darse
	// cuenta de que el movimiento no se hizo.
	@ExceptionHandler(StockInsuficienteException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public String stockInsuficiente(StockInsuficienteException e) {
		return e.getMessage();
	}
	
	//3- cuando el DTO no pasa las validaciones -> 400 con el campo que fallo
	//1b- cuando se rompe una regla del negocio -> 400 con el motivo
	@ExceptionHandler(ReglaNegocioException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String reglaRota(ReglaNegocioException e) {
		return e.getMessage();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, String> datosInvalidos(MethodArgumentNotValidException e) {
		
		Map<String, String> errores = new HashMap<>();
		
		for (FieldError error : e.getBindingResult().getFieldErrors()) {
			errores.put(error.getField(), error.getDefaultMessage());
		}
		
		return errores;
	}

}
