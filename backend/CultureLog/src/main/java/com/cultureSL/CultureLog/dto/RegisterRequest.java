package com.cultureSL.CultureLog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para el registro de un nuevo usuario.
 * <p>
 * Incluye validaciones de formato y longitud para garantizar datos consistentes
 * antes de persistir el nuevo usuario en la base de datos.
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    @jakarta.validation.constraints.Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos"
    )
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
    @jakarta.validation.constraints.Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "La contraseña debe contener al menos una mayúscula, una minúscula y un número"
    )
    private String password;

    /** Dirección de correo electrónico (formato válido, obligatorio). */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;
}