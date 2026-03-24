package com.cultureSL.CultureLog.service.search;

import com.cultureSL.CultureLog.dto.MediaItemResponse;
import com.cultureSL.CultureLog.dto.search.MediaSearchResult;
import com.cultureSL.CultureLog.model.enums.MediaType;

import java.util.List;

/**
 * Servicio fachada para la búsqueda de contenido multimedia en APIs externas.
 * <p>
 * Orquesta las llamadas a los distintos proveedores ({@link ExternalMediaProvider})
 * y permite añadir los resultados encontrados a la biblioteca personal del usuario,
 * subiendo la imagen de portada a Cloudinary en el proceso.
 * </p>
 *
 * @see ExternalMediaProvider
 */
public interface MediaSearchService {

    /**
     * Busca contenido multimedia en las APIs externas configuradas.
     * <p>
     * Si se especifica un tipo, solo se consultan los proveedores que lo soporten.
     * Si no se especifica, se consultan todos los proveedores en paralelo.
     * </p>
     *
     * @param query texto de búsqueda del usuario
     * @param type  filtro opcional por tipo de medio ({@code null} para buscar en todos)
     * @param page  número de página de resultados (base 0)
     * @return lista unificada de resultados normalizados de todas las fuentes consultadas
     */
    List<MediaSearchResult> search(String query, MediaType type, int page);

    /**
     * Añade un resultado de búsqueda externa a la biblioteca personal del usuario.
     * <p>
     * Sube la imagen de portada a Cloudinary (si existe) y crea el ítem multimedia
     * con estado inicial {@code POR_VER}.
     * </p>
     *
     * @param userId       ID del usuario autenticado
     * @param searchResult resultado de búsqueda a convertir en ítem de biblioteca
     * @return el ítem creado en formato {@link MediaItemResponse}
     */
    MediaItemResponse addToLibrary(Long userId, MediaSearchResult searchResult);
}
