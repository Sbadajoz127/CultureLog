package com.cultureSL.CultureLog.service.search.provider;

import com.cultureSL.CultureLog.dto.search.MediaSearchResult;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.service.search.ExternalMediaProvider;
import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Proveedor de búsqueda de videojuegos en RAWG Video Games Database.
 * <p>
 * Utiliza el endpoint {@code /games} de la API de RAWG para buscar videojuegos
 * por nombre. Extrae la imagen de fondo ({@code background_image}) como imagen
 * representativa del juego. Requiere una API key gratuita.
 * </p>
 *
 * @see <a href="https://rawg.io/apidocs">RAWG API Docs</a>
 */
@Component
@Slf4j
public class RawgSearchProvider implements ExternalMediaProvider {

    private final RestClient restClient;
    private final String apiKey;

    public RawgSearchProvider(@Qualifier("rawgRestClient") RestClient restClient,
                              @Value("${api.rawg.key}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public Set<MediaType> getSupportedTypes() {
        return Set.of(MediaType.VIDEOJUEGO);
    }

    @Override
    public List<MediaSearchResult> search(String query, int page) {
        try {
            JsonNode response = restClient.get()
                    .uri("/games?key={key}&search={q}&page={p}&page_size=10",
                            apiKey, query, page + 1)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("results")) {
                return Collections.emptyList();
            }

            List<MediaSearchResult> results = new ArrayList<>();
            for (JsonNode item : response.get("results")) {
                String title = item.path("name").asText("");
                if (title.isBlank()) continue;

                String imageUrl = item.path("background_image").asText(null);

                List<String> genres = new ArrayList<>();
                JsonNode genresNode = item.path("genres");
                if (genresNode.isArray()) {
                    for (JsonNode g : genresNode) {
                        genres.add(g.path("name").asText());
                    }
                }

                LocalDate releaseDate = parseDate(item.path("released").asText(null));

                Double rating = item.has("rating") && !item.path("rating").isNull()
                        ? item.path("rating").asDouble() : null;

                results.add(MediaSearchResult.builder()
                        .externalId(String.valueOf(item.path("id").asLong()))
                        .source("RAWG")
                        .title(title)
                        .type(MediaType.VIDEOJUEGO)
                        .genre(String.join(", ", genres))
                        .description(null)
                        .releaseDate(releaseDate)
                        .imageUrl(imageUrl)
                        .rating(rating)
                        .build());
            }
            return results;
        } catch (Exception e) {
            log.error("Error buscando en RAWG: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Parsea una fecha en formato ISO (yyyy-MM-dd) de forma segura.
     *
     * @param dateStr cadena de fecha; puede ser {@code null} o vacía
     * @return fecha parseada o {@code null} si no es válida
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
