package com.cultureSL.CultureLog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta utilizado tras operaciones de autenticación (Login o Registro).
 * <p>
 * Devuelve al cliente la información básica del usuario autenticado y un mensaje de estado,
 * permitiendo al frontend iniciar la sesión localmente.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    /** ID único del usuario en la base de datos. */
    private Long id;
    /** Nombre de usuario. */
    private String username;
    /** Correo electrónico registrado. */
    private String email;
    /** URL de la foto de perfil del usuario (puede ser null). */
    private String profilePictureUrl;
    /** URL del banner del usuario (puede ser null). */
    private String bannerUrl;
    /** Token JWT para autenticación en endpoints protegidos. */
    private String token;
    /** Rol del usuario (USER o ADMIN). */
    private String role;
    /** Mensaje informativo sobre el resultado de la operación (ej. "Login exitoso"). */
    private String message;
}