package com.cultureSL.CultureLog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para la creación de una nueva publicación en el feed.
 * <p>
 * Permite al usuario escribir contenido textual y, opcionalmente,
 * vincular un ítem multimedia de su biblioteca.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    /** Texto del post (obligatorio, máximo 2000 caracteres). */
    @NotBlank(message = "El contenido del post es obligatorio")
    @Size(max = 2000, message = "El contenido no puede exceder 2000 caracteres")
    private String content;

    /** ID del ítem multimedia a vincular (opcional, {@code null} si no aplica). */
    private Long linkedMediaItemId;
}