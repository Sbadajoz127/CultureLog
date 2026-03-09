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
    /** Nombre de usuario deseado (entre 3 y 30 caracteres, obligatorio). */
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    private String username;

    /** Contraseña en texto plano (mínimo 6 caracteres, se cifrará antes de almacenar). */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    /** Dirección de correo electrónico (formato válido, obligatorio). */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;
}