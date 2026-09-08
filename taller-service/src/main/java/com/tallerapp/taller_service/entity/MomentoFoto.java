package com.tallerapp.taller_service.entity;

// Cuando se saco la foto. Es lo que hace que el anexo sirva:
// las de INGRESO documentan como llego el auto y son la defensa
// ante un reclamo posterior.
public enum MomentoFoto {
	
	INGRESO,       // como llego el vehiculo: rayones previos, tablero, estado general
	DIAGNOSTICO,   // el problema encontrado
	TRABAJO,       // durante la reparacion
	ENTREGA        // como se entrega

}
