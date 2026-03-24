package com.cultureSL.CultureLog.service.search.provider;

import com.cultureSL.CultureLog.dto.search.MediaSearchResult;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.service.search.ExternalMediaProvider;
import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Clase base para los proveedores de búsqueda en Jikan (wrapper de MyAnimeList).
 * <p>
 * Encapsula la lógica compartida de comunicación con la API v4 de Jikan,
 * parseo de respuestas JSON y normalización de resultados.
 * Las subclases concretas definen el endpoint y el tipo de medio que resuelven.
 * </p>
 *
 * @see <a href="https://docs.jikan.moe/">Jikan API Docs</a>
 */
@Slf4j
public abstract class AbstractJikanSearchProvider implements ExternalMediaProvider {

    protected final RestClient restClient;
    protected final String baseUrl;

    protected AbstractJikanSearchProvider(RestClient restClient, String baseUrl) {
        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    /**
     * Endpoint de la API Jikan a consultar ({@code "anime"} o {@code "manga"}).
     */
    protected abstract String getEndpoint();

    /**
     * Tipo de medio al que corresponden los resultados de este provider.
     */
    protected abstract MediaType getMediaType();

    @Override
    public List<MediaSearchResult> search(String query, int page) {
        try {
            JsonNode response = restClient.get()
                    .uri(baseUrl + "/{endpoint}?q={q}&page={p}&limit=10",
                            getEndpoint(), query, page + 1)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("data")) {
                return Collections.emptyList();
            }

            List<MediaSearchResult> results = new ArrayList<>();
            for (JsonNode item : response.get("data")) {
                String title = item.path("title").asText("");
                if (title.isBlank()) continue;

                String imageUrl = item.path("images").path("jpg").path("large_image_url").asText(null);
                if (imageUrl == null) {
                    imageUrl = item.path("images").path("jpg").path("image_url").asText(null);
                }

                List<String> genres = new ArrayList<>();
                JsonNode genresNode = item.path("genres");
                if (genresNode.isArray()) {
                    for (JsonNode g : genresNode) {
                        genres.add(g.path("name").asText());
                    }
                }

                String creator = extractCreator(item);
                LocalDate releaseDate = parseAiredDate(item);

                Double rating = item.has("score") && !item.path("score").isNull()
                        ? item.path("score").asDouble() : null;

                results.add(MediaSearchResult.builder()
                        .externalId(String.valueOf(item.path("mal_id").asLong()))
                        .source("JIKAN")
                        .title(title)
                        .type(getMediaType())
                        .genre(String.join(", ", genres))
                        .creator(creator)
                        .description(item.path("synopsis").asText(null))
                        .releaseDate(releaseDate)
                        .imageUrl(imageUrl)
                        .rating(rating)
                        .build());
            }
            return results;
        } catch (Exception e) {
            log.error("Error buscando en Jikan ({}): {}", getEndpoint(), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Extrae el creador principal del nodo JSON: autor para manga, estudio para anime.
     */
    private String extractCreator(JsonNode item) {
        JsonNode authors = item.path("authors");
        if (authors.isArray() && !authors.isEmpty()) {
            return authors.get(0).path("name").asText(null);
        }
        JsonNode studios = item.path("studios");
        if (studios.isArray() && !studios.isEmpty()) {
            return studios.get(0).path("name").asText(null);
        }
        return null;
    }

    /**
     * Extrae la fecha de emisión/publicación del campo {@code aired} o {@code published}.
     *
     * @param item nodo JSON del anime o manga
     * @return fecha de inicio, o {@code null} si no está disponible
     */
    private LocalDate parseAiredDate(JsonNode item) {
        JsonNode aired = item.path("aired");
        if (aired.isMissingNode()) {
            aired = item.path("published");
        }
        String from = aired.path("from").asText(null);
        if (from == null || from.isBlank()) return null;
        try {
            return OffsetDateTime.parse(from).toLocalDate();
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(from.substring(0, 10));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
