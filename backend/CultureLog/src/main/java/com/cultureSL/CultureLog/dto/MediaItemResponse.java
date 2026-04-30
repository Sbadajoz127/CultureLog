package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO de salida que representa un ítem multimedia de la biblioteca del usuario.
 * <p>
 * Incluye toda la información relevante del ítem, incluyendo los nombres
 * de las etiquetas asociadas para su visualización en la interfaz.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaItemResponse {
    /** Identificador único del ítem. */
    private Long id;
    /** Título de la obra. */
    private String title;
    /** URL de la imagen representativa (portada, póster). */
    private String itemImageUrl;
    /** Tipo de medio (PELICULA, SERIE, LIBRO, etc.). */
    private MediaType type;
    /** Estado de consumo (POR_VER, EN_PROGRESO, VISTO, ABANDONADO). */
    private MediaStatus status;
    /** Género de la obra. */
    private String genre;
    /** Creador principal (autor, director, desarrollador). */
    private String creator;
    /** Puntuación personal del usuario. */
    private Integer rating;
    /** Fecha de lanzamiento original. */
    private LocalDate releaseDate;
    /** Fecha en que se añadió a la biblioteca. */
    private LocalDate dateAdded;
    /** Reseña o notas personales. */
    private String comment;
    /** Sinopsis o descripción de la obra. */
    private String description;
    /** Identificador del ítem en la API externa de origen. */
    private String externalId;
    /** Nombre de la fuente externa (TMDB, GOOGLE_BOOKS, JIKAN, RAWG). */
    private String externalSource;
    /** Nombre del álbum (solo para contenido musical). */
    private String album;
    /** Nombres de las etiquetas asociadas al ítem. */
    private Set<String> tagNames;
}
