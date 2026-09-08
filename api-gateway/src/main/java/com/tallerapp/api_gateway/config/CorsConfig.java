package com.tallerapp.api_gateway.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

	// El front pega unicamente al gateway, asi que el permiso para el navegador
	// se da aca y no en cada servicio. Si estuviera en los dos lados, la respuesta
	// llegaria con el header Access-Control-Allow-Origin repetido y el navegador
	// la rechaza igual.

	/**
	 * Va primero de todos, antes que el filtro del token.
	 *
	 * Si corre despues, las respuestas de error del JwtFiltro (un 401 por
	 * sesion vencida, un 403 por permisos) salen sin las cabeceras de CORS.
	 * El navegador entonces no las deja leer y el front recibe un
	 * "Failed to fetch" en vez del codigo: nunca se entera de que la sesion
	 * vencio y se queda mostrando tablas vacias sin decir por que.
	 */
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {

		CorsConfiguration configuracion = new CorsConfiguration();
		configuracion.addAllowedOriginPattern("*");
		configuracion.addAllowedMethod("*");
		configuracion.addAllowedHeader("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuracion);

		FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(source));
		registro.setOrder(Ordered.HIGHEST_PRECEDENCE);

		return registro;
	}

}
