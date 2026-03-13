package com.culturesl.model;

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
    /** Token JWT para autenticación en endpoints protegidos. */
    private String token;
    /** Mensaje informativo sobre el resultado de la operación (ej. "Login exitoso"). */
    private String message;
}