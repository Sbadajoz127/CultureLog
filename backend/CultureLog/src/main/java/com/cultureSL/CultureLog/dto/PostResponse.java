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
    // --- Datos del Post ---
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    
    // --- Datos del Autor ---
    private Long authorId;
    private String authorName;
    
    // --- Datos del Item Multimedia Vinculado (Resumen) ---
    private Long linkedItemId;
    private String linkedItemTitle;
    private String linkedItemType;
    private Integer linkedItemRating;

    // --- Datos Sociales ---
    private int likeCount;
    private int commentCount;
    /** Indica si el usuario que ve el feed ya ha dado like a este post (para pintar el corazón rojo/gris). */
    private boolean likedByCurrentUser;

    /** Lista breve de comentarios recientes para vista previa. */
    private List<CommentResponse> recentComments;
}