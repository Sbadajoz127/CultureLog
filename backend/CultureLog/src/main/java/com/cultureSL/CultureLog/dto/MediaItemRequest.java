package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    /** Creador principal (Autor, Director, Desarrollador). */
    private String creator;
    /** Puntuación personal del usuario (1-10). */
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 10, message = "La puntuación máxima es 10")
    private Integer rating;
    /** Reseña o notas personales. */
    private String comment;
    /** Fecha de lanzamiento original de la obra. */
    private LocalDate releaseDate;
    /** Sinopsis o descripción de la obra. */
    private String description;
    /** Identificador del ítem en la API externa de origen. */
    private String externalId;
    /** Nombre de la fuente externa (TMDB, GOOGLE_BOOKS, JIKAN, RAWG). */
    private String externalSource;
    /** Nombre del álbum (solo para contenido musical). */
    private String album;
    /** Indica si el ítem es personalizado (creado manualmente, no desde APIs externas). */
    private Boolean custom;
}