package com.cultureSL.CultureLog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para la creación de un nuevo comentario.
 * <p>
 * Contiene únicamente el texto del comentario, ya que el autor y el post
 * se infieren del contexto de la petición (ID de usuario en sesión y ID del post en la URL).
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    /** Contenido textual del comentario. */
    private String text;
}