package com.tallerapp.usuarios_service.service;

import com.tallerapp.usuarios_service.dto.DatosCobroDTO;
import com.tallerapp.usuarios_service.entity.ConfiguracionTaller;

public interface IConfiguracionService {
	
	public ConfiguracionTaller getConfiguracion();
	public String editConfiguracion(ConfiguracionTaller configuracion);
	public DatosCobroDTO getDatosCobro();

}
