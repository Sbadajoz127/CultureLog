package com.culturesl.api;

import com.culturesl.dto.PostResponse;
import com.fasterxml.jackson.core.type.TypeReference; // Necesario para Listas
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    
    // 1. SINGLETON (Añadido para que funcione tu UI)
    private static ApiClient instance;

    private final HttpClient client;
    private final ObjectMapper mapper;

    // Constructor privado para Singleton
    private ApiClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    // --- MÉTODOS DE LA API ---

    /**
     * Obtiene el feed de publicaciones.
     * NOTA: Ahora mismo devuelve DATOS FALSOS para probar el diseño.
     */
    public List<PostResponse> getFeed(int page) throws Exception {
        
        // --- OPCIÓN A: MODO DISEÑO (DATOS MOCK) ---
        // Usamos esto ahora para que veas la interfaz estilo "MediaHub" funcionando ya.
        List<PostResponse> mockPosts = new ArrayList<>();

        // Post 1: Laura88
        PostResponse p1 = new PostResponse();
        p1.setId(1L);
        p1.setAuthorName("Laura88");
        p1.setContent("¡Acabo de ver 'Blade Runner' ¡Brutal! 🎬 ¿Cuál es la mejor película de ciencia ficción para ti?");
        p1.setCreatedAt(LocalDateTime.now().minusHours(2));
        p1.setLinkedItemId(100L);
        p1.setLinkedItemTitle("Blade Runner");
        p1.setLinkedItemType("PELICULA");
        p1.setLinkedItemRating(5);
        p1.setLikeCount(210);
        p1.setCommentCount(52);
        mockPosts.add(p1);

        // Post 2: Mike_Lens
        PostResponse p2 = new PostResponse();
        p2.setId(2L);
        p2.setAuthorName("Mike_Lens");
        p2.setContent("Nueva sesión en la montaña 📸. La luz del atardecer fue increíble hoy.");
        p2.setCreatedAt(LocalDateTime.now().minusHours(4));
        p2.setLikeCount(120);
        p2.setCommentCount(36);
        mockPosts.add(p2);

        // Simulamos retardo de red para ver el efecto de carga
        Thread.sleep(800); 
        return mockPosts;

        // --- OPCIÓN B: MODO REAL (BACKEND) ---
        // Cuando tengas el backend listo, borra lo de arriba y descomenta esto:
        /*
        return get("/posts/feed?page=" + page, new TypeReference<List<PostResponse>>(){});
        */
    }

    // --- MÉTODOS GENÉRICOS HTTP (POST, GET) ---

    public <T> T post(String endpoint, Object bodyObject, Class<T> responseClass) throws Exception {
        String jsonBody = mapper.writeValueAsString(bodyObject);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return sendRequest(request, responseClass);
    }

    // Método GET Genérico (Lo necesitarás para el feed real)
    // Usamos TypeReference para poder recibir Listas (List<PostResponse>)
    public <T> T get(String endpoint, TypeReference<T> typeReference) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return mapper.readValue(response.body(), typeReference);
        } else {
            throw new Exception("Error API " + response.statusCode());
        }
    }

    // Método auxiliar privado
    private <T> T sendRequest(HttpRequest request, Class<T> responseClass) throws Exception {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return mapper.readValue(response.body(), responseClass);
        } else {
            throw new Exception("Error " + response.statusCode() + ": " + response.body());
        }
    }
}