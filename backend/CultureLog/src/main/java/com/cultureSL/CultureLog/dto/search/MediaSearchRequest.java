package com.cultureSL.CultureLog.dto.search;

import com.cultureSL.CultureLog.model.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de entrada para realizar búsquedas de contenido multimedia en APIs externas.
 * <p>
 * Permite especificar el texto de búsqueda, un filtro opcional por tipo de medio
 * y la página de resultados deseada.
 * </p>
 */
@Data
public class MediaSearchRequest {

    /** Texto de búsqueda introducido por el usuario (obligatorio). */
    @NotBlank(message = "La consulta de búsqueda es obligatoria")
    private String query;

    /** Filtro opcional por tipo de medio (PELICULA, SERIE, LIBRO, etc.). */
    private MediaType type;

    /** Número de página de resultados (base 0). */
    private int page = 0;
}
