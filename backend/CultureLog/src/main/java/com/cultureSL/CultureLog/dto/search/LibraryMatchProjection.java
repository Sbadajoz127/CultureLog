package com.cultureSL.CultureLog.dto.search;

import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;

/**
 * Proyección ligera para cruzar resultados de búsqueda externa con la biblioteca del usuario.
 * <p>
 * Devuelve solo los campos necesarios para el matching, evitando cargar
 * entidades completas con relaciones (tags, etc.).
 * </p>
 */
public interface LibraryMatchProjection {
    String getExternalId();
    String getExternalSource();
    MediaType getType();
    MediaStatus getStatus();
}
