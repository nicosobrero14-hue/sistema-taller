package com.tallerapp.taller_service.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// En la base solo va el nombre del archivo; la imagen vive en una carpeta.
// Guardarlas como BLOB infla la base y hace lentos los backups.
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "fotos_orden", indexes = @Index(name = "idx_foto_orden", columnList = "orden_id"))
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class FotoOrden {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_foto;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orden_id", nullable = false)
	private OrdenTrabajo ordenTrabajo;
	
	@Column(nullable = false)
	private String archivo;
	
	// version chica de la misma imagen, para que la galeria no tenga que
	// bajar 4 MB por foto
	private String miniatura;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MomentoFoto momento;
	
	private String descripcion;
	private LocalDateTime fecha;
	
	// quien la saco, referencia a usuarios-service
	private Long id_empleado;

}
