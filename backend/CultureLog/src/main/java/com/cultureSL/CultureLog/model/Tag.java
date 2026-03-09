package com.cultureSL.CultureLog.model;

import com.cultureSL.CultureLog.model.enums.TagColor;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa una etiqueta o categoría para clasificar items multimedia.
 * <p>
 * Cada etiqueta tiene un nombre único y un color asociado para su representación visual en la interfaz.
 * </p>
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Nombre de la etiqueta (ej: "Favoritos", "Verano 2024"). Debe ser único. */
    @Column(nullable = false, unique = true)
    private String name;

    /** Color visual de la etiqueta. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagColor color = TagColor.POR_DEFECTO;

    /**
     * Crea una etiqueta con el nombre indicado y el color por defecto.
     *
     * @param name nombre de la etiqueta
     */
    public Tag(String name) {
        this.name = name;
        this.color = TagColor.POR_DEFECTO;
    }
}