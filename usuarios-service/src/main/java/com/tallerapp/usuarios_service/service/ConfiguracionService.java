package com.tallerapp.usuarios_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerapp.usuarios_service.dto.DatosCobroDTO;
import com.tallerapp.usuarios_service.entity.ConfiguracionTaller;
import com.tallerapp.usuarios_service.repository.IConfiguracionRepository;

@Service
public class ConfiguracionService implements IConfiguracionService {
	
	@Autowired
	private IConfiguracionRepository configuracionRepository;
	
	
	// Siempre es la misma fila. Si todavia no existe se crea vacia,
	// asi el front nunca recibe null.
	@Override
	public ConfiguracionTaller getConfiguracion() {
		
		List<ConfiguracionTaller> todas = configuracionRepository.findAll();
		
		if (!todas.isEmpty()) {
			return todas.get(0);
		}
		
		ConfiguracionTaller configuracion = new ConfiguracionTaller();
		configuracion.setRazonSocial("");
		configuracion.setCondicionFiscal("MONOTRIBUTO");
		
		return configuracionRepository.save(configuracion);
	}

	@Override
	public String editConfiguracion(ConfiguracionTaller configuracion) {
		
		ConfiguracionTaller actual = this.getConfiguracion();
		
		actual.setRazonSocial(configuracion.getRazonSocial());
		actual.setNombreFantasia(configuracion.getNombreFantasia());
		actual.setCuit(configuracion.getCuit());
		actual.setDomicilio(configuracion.getDomicilio());
		actual.setTelefono(configuracion.getTelefono());
		actual.setEmail(configuracion.getEmail());
		actual.setCondicionFiscal(configuracion.getCondicionFiscal());
		actual.setPuntoVenta(configuracion.getPuntoVenta());
		actual.setPrecioHoraManoObra(configuracion.getPrecioHoraManoObra());
		actual.setPorcentajeIva(configuracion.getPorcentajeIva());
		
		actual.setBanco(configuracion.getBanco());
		actual.setCbu(configuracion.getCbu());
		actual.setAliasCbu(configuracion.getAliasCbu());
		actual.setTitularCuenta(configuracion.getTitularCuenta());
		actual.setDatosTarjeta(configuracion.getDatosTarjeta());
		actual.setDatosQr(configuracion.getDatosQr());
		
		configuracionRepository.save(actual);
		
		return "Datos del taller actualizados correctamente";
	}

	/**
	 * Lo que necesita el mostrador para cobrar e imprimir, sin la parte
	 * administrativa. Lo puede leer cualquier empleado.
	 */
	@Override
	public DatosCobroDTO getDatosCobro() {
		
		ConfiguracionTaller c = this.getConfiguracion();
		
		DatosCobroDTO datos = new DatosCobroDTO();
		datos.setRazonSocial(c.getRazonSocial());
		datos.setNombreFantasia(c.getNombreFantasia());
		datos.setCuit(c.getCuit());
		datos.setDomicilio(c.getDomicilio());
		datos.setTelefono(c.getTelefono());
		datos.setPorcentajeIva(c.getPorcentajeIva());
		
		datos.setBanco(c.getBanco());
		datos.setCbu(c.getCbu());
		datos.setAliasCbu(c.getAliasCbu());
		datos.setTitularCuenta(c.getTitularCuenta());
		datos.setDatosTarjeta(c.getDatosTarjeta());
		datos.setDatosQr(c.getDatosQr());
		
		return datos;
	}

}
