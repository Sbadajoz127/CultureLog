package com.cultureSL.CultureLog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para la solicitud de inicio de sesión.
 * <p>
 * Contiene las credenciales necesarias para autenticar a un usuario existente.
 * Ambos campos son obligatorios y se validan con {@code @NotBlank}.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    /** Nombre de usuario registrado. */
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    /** Contraseña en texto plano (se compara con el hash almacenado). */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}