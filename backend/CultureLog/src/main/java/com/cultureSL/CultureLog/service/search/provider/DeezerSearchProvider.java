package com.cultureSL.CultureLog.service.search.provider;

import com.cultureSL.CultureLog.dto.search.MediaSearchResult;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.service.search.ExternalMediaProvider;
import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Proveedor de búsqueda de música en Deezer.
 * <p>
 * Utiliza el endpoint público {@code /search} de la API de Deezer para buscar
 * canciones por nombre. Cada resultado incluye información del artista y del álbum
 * con su portada. No requiere API key ni autenticación.
 * </p>
 *
 * @see <a href="https://developers.deezer.com/api/search">Deezer Search API</a>
 */
@Component
@Slf4j
public class DeezerSearchProvider implements ExternalMediaProvider {

    private static final int PAGE_SIZE = 10;

    private final RestClient restClient;

    public DeezerSearchProvider(@Qualifier("deezerRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Set<MediaType> getSupportedTypes() {
        return Set.of(MediaType.MUSICA);
    }

    @Override
    public List<MediaSearchResult> search(String query, int page) {
        try {
            int index = page * PAGE_SIZE;

            JsonNode response = restClient.get()
                    .uri("/search?q={q}&index={idx}&limit={lim}", query, index, PAGE_SIZE)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("data")) {
                return Collections.emptyList();
            }

            List<MediaSearchResult> results = new ArrayList<>();
            for (JsonNode track : response.get("data")) {
                String title = track.path("title").asText("");
                if (title.isBlank()) continue;

                String artistName = track.path("artist").path("name").asText(null);
                String albumTitle = track.path("album").path("title").asText(null);
                String imageUrl = track.path("album").path("cover_big").asText(null);

                Double rating = normalizeRank(track.path("rank").asLong(0));

                results.add(MediaSearchResult.builder()
                        .externalId(String.valueOf(track.path("id").asLong()))
                        .source("DEEZER")
                        .title(title)
                        .type(MediaType.MUSICA)
                        .genre(null)
                        .creator(artistName)
                        .description(null)
                        .releaseDate(null)
                        .imageUrl(imageUrl)
                        .rating(rating)
                        .album(albumTitle)
                        .build());
            }
            return results;
        } catch (Exception e) {
            log.error("Error buscando en Deezer: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Normaliza el ranking de Deezer (0–1 000 000) a una escala 0–10.
     *
     * @param rank valor de ranking devuelto por la API
     * @return valor normalizado entre 0 y 10, o {@code null} si el rank es 0
     */
    private Double normalizeRank(long rank) {
        if (rank <= 0) return null;
        return Math.round(rank / 100_000.0 * 10.0) / 10.0;
    }
}
