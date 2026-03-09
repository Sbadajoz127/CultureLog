package com.cultureSL.CultureLog.model.enums;

/**
 * Categorías de contenido cultural soportadas por la aplicación.
 * <p>
 * Se utiliza para clasificar los items de la biblioteca y permitir filtrado específico.
 * </p>
 */
public enum MediaType {
    /** Largometraje cinematográfico. */
    PELICULA,
    /** Serie de televisión o plataforma de streaming. */
    SERIE,
    /** Libro físico o digital (novela, ensayo, cómic, etc.). */
    LIBRO,
    /** Videojuego de cualquier plataforma. */
    VIDEOJUEGO,
    /** Serie de animación japonesa. */
    ANIME,
    /** Cómic japonés (manga). */
    MANGA,
    /** Programa de audio bajo demanda. */
    PODCAST,
    /** Para cualquier obra que no encaje en las categorías anteriores (ej: Documentales, Teatro). */
    OTRO
}