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
@Table(name = "tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"})
})
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

    /** Nombre de la etiqueta (ej: "Favoritos", "Verano 2024"). Único por usuario. */
    @Column(nullable = false)
    private String name;

    /** Color visual de la etiqueta. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagColor color = TagColor.POR_DEFECTO;

    /** Usuario propietario de esta etiqueta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Crea una etiqueta con nombre, color por defecto y usuario propietario.
     *
     * @param name nombre de la etiqueta
     * @param user usuario propietario
     */
    public Tag(String name, User user) {
        this.name = name;
        this.user = user;
        this.color = TagColor.POR_DEFECTO;
    }
}