package com.tallerapp.taller_service.service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tallerapp.taller_service.entity.FotoOrden;
import com.tallerapp.taller_service.entity.MomentoFoto;
import com.tallerapp.taller_service.entity.OrdenTrabajo;
import com.tallerapp.taller_service.exception.RecursoNoEncontradoException;
import com.tallerapp.taller_service.exception.ReglaNegocioException;
import com.tallerapp.taller_service.repository.IFotoOrdenRepository;
import com.tallerapp.taller_service.repository.IOrdenTrabajoRepository;

@Service
public class FotoOrdenService implements IFotoOrdenService {
	
	@Autowired
	private IFotoOrdenRepository fotoRepository;
	
	@Autowired
	private IOrdenTrabajoRepository ordenRepository;
	
	// carpeta donde se guardan las imagenes. Configurable para poder
	// moverla despues a un disco compartido o a S3.
	@Value("${app.fotos.carpeta}")
	private String carpeta;
	
	private static final long TAMANIO_MAXIMO = 8 * 1024 * 1024; // 8 MB
	
	
	@Override
	public String saveFoto(Long id_orden, MultipartFile archivo, MomentoFoto momento, String descripcion, Long id_empleado) {
		
		OrdenTrabajo orden = ordenRepository.findById(id_orden)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden con id " + id_orden));
		
		if (archivo == null || archivo.isEmpty()) {
			throw new ReglaNegocioException("No llego ningun archivo");
		}
		
		if (archivo.getSize() > TAMANIO_MAXIMO) {
			throw new ReglaNegocioException("La foto pesa mas de 8 MB, achicala antes de subirla");
		}
		
		String tipo = archivo.getContentType();
		
		if (tipo == null || !tipo.startsWith("image/")) {
			throw new ReglaNegocioException("El archivo tiene que ser una imagen");
		}
		
		try {
			Path destino = Paths.get(carpeta);
			Files.createDirectories(destino);
			
			// nombre unico: si dos personas suben "IMG_1234.jpg" no se pisan
			String nombre = UUID.randomUUID() + this.extension(archivo.getOriginalFilename());
			
			Files.copy(archivo.getInputStream(), destino.resolve(nombre));
			
			String mini = this.generarMiniatura(destino, nombre);
			
			FotoOrden foto = new FotoOrden();
			foto.setOrdenTrabajo(orden);
			foto.setArchivo(nombre);
			foto.setMiniatura(mini);
			foto.setMomento(momento);
			foto.setDescripcion(descripcion);
			foto.setFecha(LocalDateTime.now());
			foto.setId_empleado(id_empleado);
			
			fotoRepository.save(foto);
			
			return "Foto agregada correctamente";
			
		} catch (IOException e) {
			throw new ReglaNegocioException("No se pudo guardar la foto: " + e.getMessage());
		}
	}

	@Override
	public List<FotoOrden> getFotosPorOrden(Long id_orden) {
		
		return fotoRepository.findByOrdenId(id_orden);
	}

	@Override
	public byte[] leerArchivo(String archivo) {
		
		try {
			// solo el nombre del archivo: sin esto alguien podria pedir
			// ../../algo y leer cualquier cosa del disco
			Path ruta = Paths.get(carpeta).resolve(Paths.get(archivo).getFileName());
			
			if (!Files.exists(ruta)) {
				throw new RecursoNoEncontradoException("No existe la foto " + archivo);
			}
			
			return Files.readAllBytes(ruta);
			
		} catch (IOException e) {
			throw new RecursoNoEncontradoException("No se pudo leer la foto " + archivo);
		}
	}

	@Override
	public String deleteFoto(Long id) {
		
		FotoOrden foto = fotoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la foto con id " + id));
		
		try {
			Files.deleteIfExists(Paths.get(carpeta).resolve(foto.getArchivo()));
			
			if (foto.getMiniatura() != null) {
				Files.deleteIfExists(Paths.get(carpeta).resolve(foto.getMiniatura()));
			}
		} catch (IOException e) {
			// si el archivo ya no esta, igual sacamos el registro
		}
		
		fotoRepository.deleteById(id);
		
		return "Foto eliminada correctamente";
	}
	
	/**
	 * Achica la imagen a 400px de ancho y la guarda al lado de la original.
	 * La galeria muestra esta: una foto de celular puede pesar 4 MB y la
	 * miniatura queda en unos 30 KB.
	 */
	private String generarMiniatura(Path destino, String nombre) {
		
		try {
			BufferedImage original = ImageIO.read(destino.resolve(nombre).toFile());
			
			if (original == null) {
				return null;
			}
			
			int anchoMini = 400;
			
			// si ya es chica no vale la pena achicarla
			if (original.getWidth() <= anchoMini) {
				return null;
			}
			
			int altoMini = (int) (original.getHeight() * ((double) anchoMini / original.getWidth()));
			
			Image escalada = original.getScaledInstance(anchoMini, altoMini, Image.SCALE_SMOOTH);
			BufferedImage mini = new BufferedImage(anchoMini, altoMini, BufferedImage.TYPE_INT_RGB);
			
			Graphics2D lienzo = mini.createGraphics();
			lienzo.drawImage(escalada, 0, 0, null);
			lienzo.dispose();
			
			String nombreMini = "mini-" + nombre.substring(0, nombre.lastIndexOf(".")) + ".jpg";
			ImageIO.write(mini, "jpg", destino.resolve(nombreMini).toFile());
			
			return nombreMini;
			
		} catch (Exception e) {
			// si falla se sigue sin miniatura: la galeria usa la original
			return null;
		}
	}
	
	private String extension(String nombreOriginal) {
		
		if (nombreOriginal == null || !nombreOriginal.contains(".")) {
			return ".jpg";
		}
		
		return nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
	}

}
