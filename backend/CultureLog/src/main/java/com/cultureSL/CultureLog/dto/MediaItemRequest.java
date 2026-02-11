package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO de entrada para crear o actualizar un elemento multimedia en la biblioteca.
 * <p>
 * Encapsula todos los campos editables por el usuario para una obra (título, estado, valoración, etc.).
 * </p>
 */
@Data
public class MediaItemRequest {
    /** Título de la obra. */
    private String title;
    /** Url de la imagen representativa del ítem (portada, póster, etc.). */
    private String itemImageUrl;
    /** Tipo de medio (PELICULA, LIBRO...). */
    private MediaType type;
    /** Estado de consumo (VISTO, POR_VER...). */
    private MediaStatus status;
    /** Género literario/cinematográfico. */
    private String genre;
    /** Valoración personal (ej. 1-5). */
    private Integer rating;
    /** Reseña o comentario personal. */
    private String comment;
    /** Fecha de lanzamiento de la obra original (opcional). */
    private LocalDate releaseDate;
}