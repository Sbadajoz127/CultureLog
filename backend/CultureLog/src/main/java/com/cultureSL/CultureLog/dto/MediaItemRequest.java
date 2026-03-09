package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO de entrada para crear o actualizar un ítem multimedia en la biblioteca del usuario.
 * <p>
 * Los campos obligatorios son el título, el tipo de medio y el estado.
 * El resto de campos son opcionales y permiten enriquecer la información del ítem.
 * </p>
 */
@Data
public class MediaItemRequest {
    /** Título de la obra (obligatorio). */
    @NotBlank(message = "El título es obligatorio")
    private String title;

    /** URL de la imagen representativa del ítem (portada, póster). */
    private String itemImageUrl;

    /** Tipo de medio: PELICULA, SERIE, LIBRO, etc. (obligatorio). */
    @NotNull(message = "El tipo de medio es obligatorio")
    private MediaType type;

    /** Estado de consumo: POR_VER, EN_PROGRESO, VISTO, ABANDONADO (obligatorio). */
    @NotNull(message = "El estado es obligatorio")
    private MediaStatus status;

    /** Género de la obra (ej: "Ciencia Ficción", "Terror"). */
    private String genre;
    /** Puntuación personal del usuario. */
    private Integer rating;
    /** Reseña o notas personales. */
    private String comment;
    /** Fecha de lanzamiento original de la obra. */
    private LocalDate releaseDate;
}