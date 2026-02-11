package com.cultureSL.CultureLog.model;

import com.cultureSL.CultureLog.model.enums.TagColor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una etiqueta o categoría para clasificar items multimedia.
 * <p>
 * Cada etiqueta tiene un nombre único y un color asociado para su representación visual en la interfaz.
 * </p>
 */
@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la etiqueta (ej: "Favoritos", "Verano 2024"). Debe ser único. */
    @Column(nullable = false, unique = true)
    private String name;

    /** Color visual de la etiqueta. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagColor color = TagColor.POR_DEFECTO;

    public Tag(String name) {
        this.name = name;
        this.color = TagColor.POR_DEFECTO;
    }
}