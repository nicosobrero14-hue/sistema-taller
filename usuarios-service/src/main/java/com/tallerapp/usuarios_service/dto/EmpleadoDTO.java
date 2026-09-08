package com.tallerapp.usuarios_service.dto;

import com.tallerapp.usuarios_service.entity.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter 
@NoArgsConstructor @AllArgsConstructor
public class EmpleadoDTO {
	
	private Long id;

    @NotBlank
    private String nombre;

    private String apellido;

    @Email
    private String email;

    // solo se usa al crear el empleado; para cambiarla despues
    // esta el endpoint /auth/password
    private String password;

    @NotNull
    private Rol rol;

    @NotNull
    private Long id_sucursal;

    private String sucursalNombre;
    
}
