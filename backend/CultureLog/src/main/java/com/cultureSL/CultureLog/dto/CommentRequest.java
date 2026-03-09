package com.cultureSL.CultureLog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para añadir un comentario a una publicación.
 * <p>
 * El texto es obligatorio y está limitado a 1000 caracteres.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    /** Contenido textual del comentario (obligatorio, máximo 1000 caracteres). */
    @NotBlank(message = "El texto del comentario es obligatorio")
    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String text;
}