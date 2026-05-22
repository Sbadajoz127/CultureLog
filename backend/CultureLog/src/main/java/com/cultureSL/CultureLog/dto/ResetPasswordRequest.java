package com.cultureSL.CultureLog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para restablecer la contraseña mediante un token de seguridad.
 * <p>
 * Valida que tanto el token como la nueva contraseña estén presentes
 * y que la contraseña cumpla con los requisitos mínimos de longitud.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
    @jakarta.validation.constraints.Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "La contraseña debe contener al menos una mayúscula, una minúscula y un número"
    )
    private String newPassword;
}
