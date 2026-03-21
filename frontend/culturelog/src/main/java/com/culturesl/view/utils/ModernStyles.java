package com.culturesl.view.utils;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ModernStyles {
    // Paleta de colores (En JavaFX se usa Color.rgb en lugar de new Color)
    public static final Color BG_MAIN = Color.rgb(18, 18, 18);       // Fondo Negro Total
    public static final Color BG_PANEL = Color.rgb(30, 33, 39);      // Fondo Tarjetas (Gris azulado oscuro)
    public static final Color ACCENT = Color.rgb(68, 138, 255);      // Azul Brillante
    public static final Color TEXT_PRIMARY = Color.rgb(255, 255, 255);
    public static final Color TEXT_SECONDARY = Color.rgb(170, 170, 170);
    public static final Color RED_HEART = Color.rgb(229, 57, 53);
    
    // Fuentes (En JavaFX se usa Font.font para definir familia, peso y tamaño)
    public static final Font FONT_TITLE = Font.font("Segoe UI", FontWeight.BOLD, 22);
    public static final Font FONT_HEADER = Font.font("Segoe UI", FontWeight.BOLD, 16);
    public static final Font FONT_NORMAL = Font.font("Segoe UI", FontWeight.NORMAL, 14);
    public static final Font FONT_SMALL = Font.font("Segoe UI", FontWeight.NORMAL, 12);

    // Método extra muy útil para JavaFX: Convierte un Color a formato Hexadecimal (ej: #121212)
    // Esto te servirá muchísimo para inyectar estos colores en los .setStyle("-fx-background-color: ...")
    public static String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}