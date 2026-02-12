package com.culturesl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de salida para visualizar un comentario en la interfaz de usuario.
 * <p>
 * Incluye los datos del comentario y la información esencial del autor para
 * mostrar su nombre sin exponer datos sensibles.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    /** ID del comentario. */
    private Long id;
    /** Texto del comentario. */
    private String text;
    /** Nombre de usuario del autor. */
    private String authorName;
    /** ID del autor (útil para navegar a su perfil). */
    private Long authorId;
    /** Fecha de creación. */
    private LocalDateTime createdAt;
}