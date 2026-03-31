package com.cultureSL.CultureLog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuración de los clientes HTTP utilizados para las llamadas a APIs externas.
 * <p>
 * Proporciona beans {@link RestClient} especializados por proveedor con baseUrl
 * y timeouts ajustados a las necesidades de cada API, además de un bean genérico
 * para usos generales (ej: Cloudinary).
 * </p>
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestFactory(buildFactory(5, 10))
                .build();
    }

    @Bean("tmdbRestClient")
    public RestClient tmdbRestClient(@Value("${api.tmdb.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(buildFactory(5, 10))
                .build();
    }

    @Bean("rawgRestClient")
    public RestClient rawgRestClient(@Value("${api.rawg.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(buildFactory(5, 10))
                .build();
    }

    @Bean("jikanRestClient")
    public RestClient jikanRestClient(@Value("${api.jikan.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(buildFactory(5, 15))
                .build();
    }

    @Bean("openLibraryRestClient")
    public RestClient openLibraryRestClient() {
        return RestClient.builder()
                .baseUrl("https://openlibrary.org")
                .requestFactory(buildFactory(5, 10))
                .build();
    }

    private SimpleClientHttpRequestFactory buildFactory(int connectSeconds, int readSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(connectSeconds));
        factory.setReadTimeout(Duration.ofSeconds(readSeconds));
        return factory;
    }
}
