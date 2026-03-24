package com.cultureSL.CultureLog.service.search.provider;

import com.cultureSL.CultureLog.model.enums.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Set;

/**
 * Proveedor de búsqueda de anime en Jikan (wrapper de MyAnimeList).
 * <p>
 * Consulta el endpoint {@code /anime} de la API v4 de Jikan.
 * Al ser un provider independiente, se ejecuta en paralelo con
 * {@link JikanMangaSearchProvider} cuando no se filtra por tipo.
 * </p>
 *
 * @see AbstractJikanSearchProvider
 */
@Component
public class JikanAnimeSearchProvider extends AbstractJikanSearchProvider {

    public JikanAnimeSearchProvider(RestClient restClient,
                                    @Value("${api.jikan.base-url}") String baseUrl) {
        super(restClient, baseUrl);
    }

    @Override
    protected String getEndpoint() {
        return "anime";
    }

    @Override
    protected MediaType getMediaType() {
        return MediaType.ANIME;
    }

    @Override
    public Set<MediaType> getSupportedTypes() {
        return Set.of(MediaType.ANIME);
    }
}
