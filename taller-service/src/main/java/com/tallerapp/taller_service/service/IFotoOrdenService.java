package com.tallerapp.taller_service.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.tallerapp.taller_service.entity.FotoOrden;
import com.tallerapp.taller_service.entity.MomentoFoto;

public interface IFotoOrdenService {
	
	public String saveFoto(Long id_orden, MultipartFile archivo, MomentoFoto momento, String descripcion, Long id_empleado);
	public List<FotoOrden> getFotosPorOrden(Long id_orden);
	public byte[] leerArchivo(String archivo);
	public String deleteFoto(Long id);

}
