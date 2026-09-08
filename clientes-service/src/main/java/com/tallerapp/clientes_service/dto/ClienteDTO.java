package com.tallerapp.clientes_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ClienteDTO {
	
	private Long id;

    @NotBlank
    private String nombre;

    private String telefono;

    @Email
    private String email;

}
