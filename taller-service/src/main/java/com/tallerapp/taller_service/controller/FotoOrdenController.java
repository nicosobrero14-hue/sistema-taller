package com.tallerapp.taller_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tallerapp.taller_service.entity.FotoOrden;
import com.tallerapp.taller_service.entity.MomentoFoto;
import com.tallerapp.taller_service.service.IFotoOrdenService;

@RestController
@RequestMapping("/fotos")
public class FotoOrdenController {
	
	@Autowired
	private IFotoOrdenService fotoServ;
	
	//1- subir una foto a una orden. Es el mismo endpoint que va a usar
	// la app movil el dia que exista: recibe el archivo tal cual.
	@PostMapping(value = "/subir", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String subirFoto (@RequestParam Long id_orden,
							 @RequestParam MultipartFile archivo,
							 @RequestParam MomentoFoto momento,
							 @RequestParam(required = false) String descripcion,
							 @RequestHeader(value = "X-Empleado-Id", required = false) Long id_empleado) {
		
		return fotoServ.saveFoto(id_orden, archivo, momento, descripcion, id_empleado);
	}
	
	//2- las fotos de una orden (solo los datos, no las imagenes)
	@GetMapping("/orden/{id_orden}")
	public List<FotoOrden> traerFotosPorOrden (@PathVariable Long id_orden) {
		return fotoServ.getFotosPorOrden(id_orden);
	}
	
	//3- la version chica, que es la que usa la galeria
	@GetMapping(value = "/mini/{archivo}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] verMiniatura (@PathVariable String archivo) {
		return fotoServ.leerArchivo(archivo);
	}
	
	//4- la imagen en si, para mostrarla en pantalla
	@GetMapping(value = "/ver/{archivo}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] verFoto (@PathVariable String archivo) {
		return fotoServ.leerArchivo(archivo);
	}
	
	//5- borrar una foto
	@DeleteMapping("/borrar/{id}")
	public String deleteFoto (@PathVariable Long id) {
		return fotoServ.deleteFoto(id);
	}

}
