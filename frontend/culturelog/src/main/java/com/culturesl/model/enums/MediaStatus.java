package com.culturesl.model.enums;

/**
 * Representa el estado de consumo de una obra multimedia en la biblioteca del usuario.
 * <p>
 * Fundamental para organizar las listas de "Pendientes" vs "Completados".
 * </p>
 */
public enum MediaStatus {
    /** El usuario tiene intención de consumir la obra en el futuro (Lista de pendientes). */
    POR_VER,
    /** El usuario está consumiendo la obra actualmente (ej: leyendo un libro, viendo una serie). */
    EN_PROGRESO,
    /** El usuario ha terminado la obra. Habilita la opción de puntuar y reseñar. */
    VISTO,
    /** El usuario empezó la obra pero decidió no terminarla. */
    ABANDONADO
}