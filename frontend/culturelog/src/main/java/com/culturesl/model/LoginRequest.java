package com.culturesl.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada con las credenciales necesarias para iniciar sesión.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    /** Nombre de usuario. */
    private String username;
    /** Contraseña en texto plano (será verificada contra el hash en base de datos). */
    private String password;
}