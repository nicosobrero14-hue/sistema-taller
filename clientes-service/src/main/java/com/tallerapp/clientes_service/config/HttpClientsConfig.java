package com.tallerapp.clientes_service.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.DeferringLoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.tallerapp.clientes_service.client.ITallerClient;

@Configuration
public class HttpClientsConfig {

	@Value("${servicios.taller.url}")
	private String urlTaller;

	@Bean
	public ITallerClient tallerClient(DeferringLoadBalancerInterceptor loadBalancer) {
		return this.cliente(ITallerClient.class, urlTaller, loadBalancer);
	}

	// El interceptor resuelve "http://taller-service" contra eureka. No usar
	// un RestClient.Builder propio con @LoadBalanced: rompe el cliente de
	// eureka. Los timeouts son cortos para fallar rapido si el otro no esta.
	private <T> T cliente(Class<T> tipo, String url, DeferringLoadBalancerInterceptor loadBalancer) {

		SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
		fabrica.setConnectTimeout(Duration.ofSeconds(2));
		fabrica.setReadTimeout(Duration.ofSeconds(5));

		RestClient rest = RestClient.builder()
									.baseUrl(url)
									.requestFactory(fabrica)
									.requestInterceptor(loadBalancer)
									.build();

		return HttpServiceProxyFactory
				.builderFor(RestClientAdapter.create(rest))
				.build()
				.createClient(tipo);
	}

}
