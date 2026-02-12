package com.culturesl.model.enums;

/**
 * Tipos de eventos que generan una notificación en el sistema.
 * <p>
 * Se utiliza para determinar qué mensaje mostrar al usuario y qué icono renderizar en la UI.
 * </p>
 */
public enum NotificationType {
    /** Alguien ha comenzado a seguir al usuario. */
    NUEVO_SEGUIDOR,
    /** Alguien ha dado "Me gusta" a una publicación del usuario. */
    LIKE_POST,
    /** Alguien ha comentado en una publicación del usuario. */
    COMENTARIO_POST,
    /** Una persona a la que el usuario sigue ha publicado nuevo contenido. */
    NUEVO_POST
}