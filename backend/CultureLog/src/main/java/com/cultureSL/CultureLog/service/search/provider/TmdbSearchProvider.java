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
 * Proveedor de búsqueda de películas y series en The Movie Database (TMDB).
 * <p>
 * Utiliza el endpoint {@code /search/multi} de la API v3 de TMDB para obtener
 * resultados de películas y series simultáneamente. Los pósters se construyen
 * a partir de la ruta relativa devuelta por la API y la base URL de imágenes.
 * Los IDs numéricos de género se resuelven a nombres en español mediante un mapa estático.
 * </p>
 *
 * @see <a href="https://developer.themoviedb.org/docs">TMDB API Docs</a>
 */
@Component
@Slf4j
public class TmdbSearchProvider implements ExternalMediaProvider {

    /** URL base para construir URLs completas de pósters de TMDB (tamaño w500). */
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    private final RestClient restClient;
    private final String apiKey;

    public TmdbSearchProvider(@Qualifier("tmdbRestClient") RestClient restClient,
                              @Value("${api.tmdb.key}") String apiKey) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    @Override
    public Set<MediaType> getSupportedTypes() {
        return Set.of(MediaType.PELICULA, MediaType.SERIE);
    }

    @Override
    public List<MediaSearchResult> search(String query, int page) {
        try {
            JsonNode response = restClient.get()
                    .uri("/search/multi?api_key={key}&query={q}&page={p}&language=es-ES",
                            apiKey, query, page + 1)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("results")) {
                return Collections.emptyList();
            }

            List<MediaSearchResult> results = new ArrayList<>();
            for (JsonNode item : response.get("results")) {
                String mediaTypeStr = item.path("media_type").asText("");
                if (!"movie".equals(mediaTypeStr) && !"tv".equals(mediaTypeStr)) {
                    continue;
                }

                MediaType type = "movie".equals(mediaTypeStr) ? MediaType.PELICULA : MediaType.SERIE;
                boolean isMovie = type == MediaType.PELICULA;

                String title = item.path(isMovie ? "title" : "name").asText("");
                String posterPath = item.path("poster_path").asText(null);
                String imageUrl = posterPath != null ? IMAGE_BASE_URL + posterPath : null;

                String dateStr = item.path(isMovie ? "release_date" : "first_air_date").asText("");
                LocalDate releaseDate = parseDate(dateStr);

                List<String> genres = resolveGenres(item.path("genre_ids"));

                results.add(MediaSearchResult.builder()
                        .externalId(String.valueOf(item.path("id").asLong()))
                        .source("TMDB")
                        .title(title)
                        .type(type)
                        .genre(String.join(", ", genres))
                        .description(item.path("overview").asText(null))
                        .releaseDate(releaseDate)
                        .imageUrl(imageUrl)
                        .rating(item.path("vote_average").asDouble(0))
                        .build());
            }
            return results;
        } catch (Exception e) {
            log.error("Error buscando en TMDB: {}", e.getMessage());
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

    /** Mapa de IDs de género de TMDB a sus nombres en español (películas + series). */
    private static final Map<Integer, String> TMDB_GENRE_MAP = Map.ofEntries(
            Map.entry(28, "Acción"), Map.entry(12, "Aventura"), Map.entry(16, "Animación"),
            Map.entry(35, "Comedia"), Map.entry(80, "Crimen"), Map.entry(99, "Documental"),
            Map.entry(18, "Drama"), Map.entry(10751, "Familia"), Map.entry(14, "Fantasía"),
            Map.entry(36, "Historia"), Map.entry(27, "Terror"), Map.entry(10402, "Música"),
            Map.entry(9648, "Misterio"), Map.entry(10749, "Romance"), Map.entry(878, "Ciencia Ficción"),
            Map.entry(53, "Thriller"), Map.entry(10752, "Bélica"), Map.entry(37, "Western"),
            Map.entry(10759, "Acción y Aventura"), Map.entry(10762, "Infantil"),
            Map.entry(10763, "Noticias"), Map.entry(10764, "Reality"),
            Map.entry(10765, "Ciencia Ficción y Fantasía"), Map.entry(10766, "Telenovela"),
            Map.entry(10767, "Talk Show"), Map.entry(10768, "Guerra y Política")
    );

    /**
     * Convierte un array JSON de IDs de género en una lista de nombres legibles.
     *
     * @param genreIds nodo JSON con el array de IDs numéricos de género
     * @return lista de nombres de género en español
     */
    private List<String> resolveGenres(JsonNode genreIds) {
        List<String> genres = new ArrayList<>();
        if (genreIds != null && genreIds.isArray()) {
            for (JsonNode id : genreIds) {
                String name = TMDB_GENRE_MAP.get(id.asInt());
                if (name != null) genres.add(name);
            }
        }
        return genres;
    }
}
