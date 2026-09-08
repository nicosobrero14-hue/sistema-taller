package com.tallerapp.taller_service.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un repuesto del presupuesto, con el precio del dia que se emitio.
// El precio queda congelado: si el deposito lo sube manaña, este
// presupuesto sigue valiendo lo que decia hasta que venza.
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "items_presupuesto")
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemPresupuesto {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id_item;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "presupuesto_id", nullable = false)
	private Presupuesto presupuesto;
	
	// el repuesto del deposito. No se reserva stock: un presupuesto
	// sin aceptar no deberia inmovilizar mercaderia.
	private Long id_repuesto;
	
	/**
	 * A que opcion del presupuesto pertenece este repuesto.
	 *
	 * En blanco significa que va en todas: son los repuestos que hay que
	 * cambiar si o si. Con un numero, el repuesto entra solo en esa opcion:
	 * asi se le ofrecen al cliente dos o tres variantes del mismo trabajo
	 * con distintas marcas y precios.
	 */
	private Integer opcion;
	
	@Column(nullable = false)
	private String descripcion;
	private Integer cantidad;
	private BigDecimal precioUnitario;

}
