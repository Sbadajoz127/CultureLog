package com.cultureSL.CultureLog.service.search;

import com.cultureSL.CultureLog.dto.search.MediaSearchResult;
import com.cultureSL.CultureLog.model.enums.MediaType;

import java.util.List;
import java.util.Set;

/**
 * Contrato que deben implementar los proveedores de búsqueda en APIs externas (patrón Strategy).
 * <p>
 * Cada implementación encapsula la lógica de comunicación con una API concreta
 * (TMDB, Open Library, Jikan, RAWG) y normaliza los resultados en {@link MediaSearchResult}.
 * </p>
 *
 * @see com.cultureSL.CultureLog.service.search.MediaSearchService
 */
public interface ExternalMediaProvider {

    /**
     * Busca contenido multimedia en la API externa.
     *
     * @param query texto de búsqueda del usuario
     * @param page  número de página (base 0)
     * @return lista de resultados normalizados; lista vacía si no hay coincidencias o si ocurre un error
     */
    List<MediaSearchResult> search(String query, int page);

    /**
     * Indica los tipos de medio que este proveedor puede resolver.
     *
     * @return conjunto de {@link MediaType} soportados por esta implementación
     */
    Set<MediaType> getSupportedTypes();
}
