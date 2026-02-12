package com.culturesl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada con los datos necesarios para registrar un nuevo usuario.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    /** Nombre de usuario deseado (debe ser único). */
    private String username;
    /** Contraseña en texto plano (será encriptada). */
    private String password;
    /** Correo electrónico (debe ser único). */
    private String email;
}