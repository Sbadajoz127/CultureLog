package com.cultureSL.CultureLog.dto.search;

import com.cultureSL.CultureLog.model.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO unificado que representa un resultado de búsqueda procedente de una API externa.
 * <p>
 * Normaliza los datos de las distintas fuentes (TMDB, Open Library, Jikan, RAWG)
 * en una estructura común que el frontend puede consumir de forma homogénea.
 * También se utiliza como payload para añadir el ítem a la biblioteca del usuario.
 * </p>
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MediaSearchResult {

    /** Identificador del recurso en la API de origen (ej. TMDB id, RAWG id). */
    private String externalId;

    /** Nombre de la fuente externa (TMDB, OPEN_LIBRARY, JIKAN, RAWG). */
    private String source;

    /** Título de la obra. */
    @NotBlank(message = "El título es obligatorio")
    private String title;

    /** Tipo de medio mapeado al enum interno (PELICULA, SERIE, LIBRO, etc.). */
    @NotNull(message = "El tipo de medio es obligatorio")
    private MediaType type;

    /** Género(s) de la obra, separados por coma cuando hay varios. */
    private String genre;

    /** Creador principal (director, autor, estudio, desarrollador). */
    private String creator;

    /** Sinopsis o descripción proporcionada por la API externa. */
    private String description;

    /** Fecha de lanzamiento o primera publicación. */
    private LocalDate releaseDate;

    /** URL de la imagen original proporcionada por la API (póster, portada, etc.). */
    private String imageUrl;

    /** Puntuación o valoración media según la fuente externa. */
    private Double rating;

    /** Nombre del álbum (solo para contenido musical). */
    private String album;

    /** Estado en la biblioteca del usuario ({@code null} si no está en su biblioteca). */
    private String libraryStatus;
}
