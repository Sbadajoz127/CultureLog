package com.culturesl.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    
    private final HttpClient client;
    private final ObjectMapper mapper;

    public ApiClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Método genérico para hacer peticiones POST.
     * @param endpoint La ruta final (ej: "/auth/login")
     * @param bodyObject El objeto que quieres enviar (ej: LoginRequest)
     * @param responseClass La clase de la respuesta que esperas recibir (ej: AuthResponse.class)
     * @return El objeto respuesta ya convertido
     */
    public <T> T post(String endpoint, Object bodyObject, Class<T> responseClass) throws Exception {
        String jsonBody = mapper.writeValueAsString(bodyObject);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return mapper.readValue(response.body(), responseClass);
        } else {
            throw new Exception("Error " + response.statusCode() + ": " + response.body());
        }
    }

    // Aquí podrás añadir métodos get(), put(), delete() en el futuro...
}