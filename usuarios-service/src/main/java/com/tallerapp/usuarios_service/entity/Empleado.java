package com.tallerapp.usuarios_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "empleados")
public class Empleado {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_empleado;
	private String nombre;
	private String apellido;
	
	@Email
    @Column(unique = true)
	private String email;
	
	// La contraseña se guarda hasheada con BCrypt, nunca en texto plano.
	// @JsonIgnore para que no se escape en ninguna respuesta.
	@JsonIgnore
	private String password;
	
	@Enumerated(EnumType.STRING)
    @NotNull
	private Rol rol;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id", nullable = false)
	private Sucursal sucursal;
	
	// Un empleado que se va del taller no se borra —tiene ordenes hechas a
	// su nombre— se bloquea. Bloqueado no puede entrar al sistema.
	// Los que ya estaban cargados vienen en null: null es activo.
	private Boolean activo;

}
