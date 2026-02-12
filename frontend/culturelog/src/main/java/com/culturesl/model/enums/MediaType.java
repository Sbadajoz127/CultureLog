package com.culturesl.model.enums;

/**
 * Categorías de contenido cultural soportadas por la aplicación.
 * <p>
 * Se utiliza para clasificar los items de la biblioteca y permitir filtrado específico.
 * </p>
 */
public enum MediaType {
    PELICULA,
    SERIE,
    LIBRO,
    VIDEOJUEGO,
    ANIME,
    MANGA,
    PODCAST,
    /** Para cualquier obra que no encaje en las categorías anteriores (ej: Documentales, Teatro). */
    OTRO
}