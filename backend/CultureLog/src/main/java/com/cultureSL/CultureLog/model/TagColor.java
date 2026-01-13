package com.cultureSL.CultureLog.model;

import lombok.Getter;

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
    POR_DEFECTO("#E0E0E0");

    private final String hexCode;

    TagColor(String hexCode) {
        this.hexCode = hexCode;
    }
}
