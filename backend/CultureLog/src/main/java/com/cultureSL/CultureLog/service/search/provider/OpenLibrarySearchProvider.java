package com.cultureSL.CultureLog.service.search.provider;

import com.cultureSL.CultureLog.dto.search.MediaSearchResult;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.service.search.ExternalMediaProvider;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Proveedor de búsqueda de libros en Open Library (Internet Archive).
 * <p>
 * Utiliza la Search API pública ({@code /search.json}) para localizar obras literarias
 * y la Covers API ({@code covers.openlibrary.org}) para obtener las portadas en tamaño
 * grande (L). No requiere API key ni autenticación.
 * </p>
 *
 * @see <a href="https://openlibrary.org/dev/docs/api/search">Open Library Search API</a>
 * @see <a href="https://openlibrary.org/dev/docs/api/covers">Open Library Covers API</a>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenLibrarySearchProvider implements ExternalMediaProvider {

    /** URL base de la API de búsqueda de Open Library. */
    private static final String BASE_URL = "https://openlibrary.org";

    /** URL base del servicio de portadas de Open Library. */
    private static final String COVERS_URL = "https://covers.openlibrary.org";

    private final RestClient restClient;

    @Override
    public Set<MediaType> getSupportedTypes() {
        return Set.of(MediaType.LIBRO);
    }

    @Override
    public List<MediaSearchResult> search(String query, int page) {
        try {
            int olPage = page + 1;

            JsonNode response = restClient.get()
                    .uri(BASE_URL + "/search.json?q={q}&page={p}&limit=10&lang=es",
                            query, olPage)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("docs")) {
                return Collections.emptyList();
            }

            List<MediaSearchResult> results = new ArrayList<>();
            for (JsonNode doc : response.get("docs")) {
                String title = doc.path("title").asText("");
                if (title.isBlank()) continue;

                String creator = extractFirstFromArray(doc, "author_name");
                String genre = extractFirstFromArray(doc, "subject");
                String imageUrl = buildCoverUrl(doc);

                Integer firstPublishYear = doc.has("first_publish_year")
                        ? doc.path("first_publish_year").asInt() : null;
                java.time.LocalDate releaseDate = firstPublishYear != null
                        ? java.time.LocalDate.of(firstPublishYear, 1, 1) : null;

                Double rating = doc.has("ratings_average") && !doc.path("ratings_average").isNull()
                        ? doc.path("ratings_average").asDouble() : null;

                String olKey = doc.path("key").asText("");

                results.add(MediaSearchResult.builder()
                        .externalId(olKey)
                        .source("OPEN_LIBRARY")
                        .title(title)
                        .type(MediaType.LIBRO)
                        .genre(genre)
                        .creator(creator)
                        .description(null)
                        .releaseDate(releaseDate)
                        .imageUrl(imageUrl)
                        .rating(rating)
                        .build());
            }
            return results;
        } catch (Exception e) {
            log.error("Error buscando en Open Library: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Construye la URL de portada a partir del cover ID o ISBN del documento.
     *
     * @param doc nodo JSON del documento de Open Library
     * @return URL de la portada en tamaño grande, o {@code null} si no hay imagen disponible
     */
    private String buildCoverUrl(JsonNode doc) {
        if (doc.has("cover_i")) {
            int coverId = doc.path("cover_i").asInt();
            return COVERS_URL + "/b/id/" + coverId + "-L.jpg";
        }

        JsonNode isbns = doc.path("isbn");
        if (isbns.isArray() && !isbns.isEmpty()) {
            return COVERS_URL + "/b/isbn/" + isbns.get(0).asText() + "-L.jpg";
        }

        return null;
    }

    /**
     * Extrae el primer valor de texto de un campo array del documento JSON.
     *
     * @param doc   nodo JSON del documento
     * @param field nombre del campo array a leer
     * @return primer valor del array, o {@code null} si el campo no existe o está vacío
     */
    private String extractFirstFromArray(JsonNode doc, String field) {
        JsonNode arr = doc.path(field);
        if (arr.isArray() && !arr.isEmpty()) {
            return arr.get(0).asText(null);
        }
        return null;
    }
}
