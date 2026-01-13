package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Guardamos el nombre del Enum (ej: "ROJO") en la base de datos
    // Usamos TagColor.POR_DEFECTO para que nunca sea null
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagColor color = TagColor.POR_DEFECTO;

    // Constructor simple (asigna color por defecto)
    public Tag(String name) {
        this.name = name;
        this.color = TagColor.POR_DEFECTO;
    }
}