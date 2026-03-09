package com.cultureSL.CultureLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO complejo que representa una "Tarjeta de Publicación" en el Feed de noticias.
 * <p>
 * Agrega información de múltiples fuentes: datos del post, resumen del autor,
 * resumen del item multimedia vinculado, estadísticas sociales y estado de interacción del usuario actual.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    /** Identificador único del post. */
    private Long id;
    /** Contenido textual de la publicación. */
    private String content;
    /** Fecha y hora de creación. */
    private LocalDateTime createdAt;

    /** ID del autor de la publicación. */
    private Long authorId;
    /** Nombre de usuario del autor. */
    private String authorName;

    /** ID del ítem multimedia vinculado ({@code null} si no hay). */
    private Long linkedItemId;
    /** Título del ítem vinculado. */
    private String linkedItemTitle;
    /** Tipo de medio del ítem vinculado (ej: "PELICULA"). */
    private String linkedItemType;
    /** Puntuación del ítem vinculado. */
    private Integer linkedItemRating;

    /** Número total de likes en la publicación. */
    private int likeCount;
    /** Número total de comentarios en la publicación. */
    private int commentCount;
    /** Indica si el usuario que ve el feed ya ha dado like a este post (para pintar el corazón rojo/gris). */
    private boolean likedByCurrentUser;

    /** Lista breve de comentarios recientes para vista previa. */
    private List<CommentResponse> recentComments;
}