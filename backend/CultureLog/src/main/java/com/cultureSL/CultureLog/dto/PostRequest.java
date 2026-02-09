package com.cultureSL.CultureLog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para la creación de una nueva publicación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    /** Texto de la publicación. */
    private String content;
    /**
     * (Opcional) ID de un MediaItem existente en la biblioteca del usuario
     * para vincularlo al post (ej. "Acabo de ver esta película").
     */
    private Long linkedMediaItemId;
}