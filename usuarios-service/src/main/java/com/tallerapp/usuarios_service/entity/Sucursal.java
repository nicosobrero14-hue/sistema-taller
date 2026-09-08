package com.tallerapp.usuarios_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
// evita que el proxy lazy de hibernate ensucie el json con "hibernateLazyInitializer"
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "sucursales")
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class Sucursal {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_sucursal;
	
	@Column(nullable = false)
	private String nombre;
	private String direccion;

}
