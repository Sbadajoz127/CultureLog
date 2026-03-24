package com.cultureSL.CultureLog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuración del cliente HTTP utilizado para las llamadas a APIs externas.
 * <p>
 * Proporciona un bean {@link RestClient} compartido con timeouts de conexión
 * y lectura configurados para evitar bloqueos prolongados.
 * </p>
 */
@Configuration
public class RestClientConfig {

    /**
     * Crea el bean {@link RestClient} con tiempos de espera predefinidos.
     *
     * @return instancia configurada de {@link RestClient}
     */
    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
