package com.culturesl.model.enums;

/**
 * Enumeración que define los temas visuales disponibles en la interfaz de usuario.
 * <p>
 * Permite al usuario personalizar la apariencia de la aplicación (Modo Claro/Oscuro).
 * </p>
 */
public enum AppTheme {
    /** Tema claro con fondo blanco y texto oscuro. */
    LIGHT,
    /** Tema oscuro con fondo gris/negro y texto claro (Recomendado para bajo consumo). */
    DARK,
    /** Sigue la configuración predeterminada del sistema operativo del usuario. */
    SYSTEM
}