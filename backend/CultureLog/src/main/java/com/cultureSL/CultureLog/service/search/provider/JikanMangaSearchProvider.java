package com.cultureSL.CultureLog.service.search.provider;

import com.cultureSL.CultureLog.model.enums.MediaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Set;

/**
 * Proveedor de búsqueda de manga en Jikan (wrapper de MyAnimeList).
 * <p>
 * Consulta el endpoint {@code /manga} de la API v4 de Jikan.
 * Al ser un provider independiente, se ejecuta en paralelo con
 * {@link JikanAnimeSearchProvider} cuando no se filtra por tipo.
 * </p>
 *
 * @see AbstractJikanSearchProvider
 */
@Component
public class JikanMangaSearchProvider extends AbstractJikanSearchProvider {

    public JikanMangaSearchProvider(@Qualifier("jikanRestClient") RestClient restClient) {
        super(restClient);
    }

    @Override
    protected String getEndpoint() {
        return "manga";
    }

    @Override
    protected MediaType getMediaType() {
        return MediaType.MANGA;
    }

    @Override
    public Set<MediaType> getSupportedTypes() {
        return Set.of(MediaType.MANGA);
    }
}
