package com.tallerapp.clientes_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
// evita que el proxy lazy de hibernate ensucie el json con "hibernateLazyInitializer"
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "clientes", indexes = @Index(name = "idx_cliente_nombre", columnList = "nombre"))
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_cliente;
	
	@Column(nullable = false)
	private String nombre;
	private String telefono;
	
	@Email
	private String email;

}
