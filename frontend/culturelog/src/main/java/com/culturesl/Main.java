package com.culturesl;

import com.culturesl.api.AuthService;
import com.culturesl.dto.AuthResponse;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Iniciando Cliente ---");

        AuthService authService = new AuthService();

        // 1. Intento de Registro
        try {
            System.out.println("Intentando registrar usuario...");
            AuthResponse regResponse = authService.register("UsuarioSwing", "123456", "swing@test.com");
            System.out.println("Registro Éxitoso: " + regResponse.getMessage());
        } catch (Exception e) {
            System.out.println("Fallo en registro (puede que ya exista): " + e.getMessage());
        }

        // 2. Intento de Login
        try {
            System.out.println("Intentando hacer login...");
            AuthResponse loginResponse = authService.login("UsuarioSwing", "123456");
            
            System.out.println("Login Éxitoso!");
            System.out.println("ID Usuario: " + loginResponse.getId());
            System.out.println("Email: " + loginResponse.getEmail());
            
        } catch (Exception e) {
            System.err.println("Error Fatal en Login: " + e.getMessage());
        }
    }
}