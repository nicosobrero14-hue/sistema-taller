package com.tallerapp.usuarios_service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.tallerapp.usuarios_service.entity.Empleado;
import com.tallerapp.usuarios_service.entity.Rol;
import com.tallerapp.usuarios_service.entity.Sucursal;
import com.tallerapp.usuarios_service.repository.IEmpleadoRepository;
import com.tallerapp.usuarios_service.repository.ISucursalRepository;

// Si no hay ningun empleado que pueda entrar al sistema, crea uno.
// Sin esto, despues de agregar el login nadie podria loguearse nunca.
@Component
public class AdminInicial implements CommandLineRunner {
	
	@Autowired
	private IEmpleadoRepository empleadoRepository;
	
	@Autowired
	private ISucursalRepository sucursalRepository;
	
	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	
	
	@Override
	public void run(String... args) {
		
		// si ya hay alguien con contraseña, no hacemos nada
		boolean hayAlguienQuePuedeEntrar = empleadoRepository.findAll().stream()
				.anyMatch(e -> e.getPassword() != null);
		
		if (hayAlguienQuePuedeEntrar) {
			return;
		}
		
		Sucursal sucursal = sucursalRepository.findAll().stream().findFirst().orElse(null);
		
		if (sucursal == null) {
			sucursal = new Sucursal();
			sucursal.setNombre("Casa Central");
			sucursal.setDireccion("");
			sucursal = sucursalRepository.save(sucursal);
		}
		
		Empleado admin = empleadoRepository.findByEmail("admin@taller.com").orElse(new Empleado());
		admin.setNombre("Admin");
		admin.setApellido("Del Sistema");
		admin.setEmail("admin@taller.com");
		admin.setPassword(encoder.encode("admin123"));
		admin.setRol(Rol.ADMIN);
		admin.setSucursal(sucursal);
		
		empleadoRepository.save(admin);
		
		System.out.println("========================================================");
		System.out.println("  Usuario inicial creado para poder entrar al sistema:");
		System.out.println("     admin@taller.com / admin123");
		System.out.println("  Cambiala apenas entres.");
		System.out.println("========================================================");
	}

}
