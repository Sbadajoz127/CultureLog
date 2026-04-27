package com.cultureSL.CultureLog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para verificar el correo electrónico mediante un token de seguridad.
 * <p>
 * Valida que el token esté presente en la petición.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailRequest {
    @NotBlank(message = "El token es obligatorio")
    private String token;
}
