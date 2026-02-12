package com.culturesl.model.enums;

import lombok.Getter;

/**
 * Paleta de colores predefinida para las etiquetas (Tags).
 * <p>
 * Asocia un nombre legible (ej: ROJO) con su código Hexadecimal para el renderizado en la interfaz gráfica.
 * </p>
 */
@Getter
public enum TagColor {
    ROJO("#FF5252"),
    AZUL("#448AFF"),
    VERDE("#69F0AE"),
    AMARILLO("#FFD740"),
    NARANJA("#FFAB40"),
    MORADO("#E040FB"),
    ROSA("#FF4081"),
    CYAN("#18FFFF"),
    GRIS("#9E9E9E"),
    NEGRO("#212121"),
    /** Color gris claro estándar para etiquetas sin clasificación específica. */
    POR_DEFECTO("#E0E0E0");

    /** Código de color en formato Hexadecimal (ej: #FF0000) para uso en Swing/CSS. */
    private final String hexCode;

    TagColor(String hexCode) {
        this.hexCode = hexCode;
    }
}