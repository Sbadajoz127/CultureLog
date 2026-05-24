package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;

/**
 * Representa un elemento multimedia (ítem) dentro de la biblioteca personal del usuario.
 * <p>
 * Almacena información sobre películas, libros, videojuegos, etc., incluyendo
 * el estado de consumo (Visto, Por ver) y la valoración personal.
 * </p>
 */
@Entity
@Table(name = "media_items",
    indexes = {
        @Index(name = "idx_media_user_external", columnList = "user_id, externalId, externalSource, type")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_media_user_external_type",
                columnNames = {"user_id", "externalId", "externalSource", "type"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MediaItem {

    /** Identificador único del ítem multimedia. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Título de la obra. */
    @Column(nullable = false)
    private String title;

    /** Tipo de medio (LIBRO, PELICULA, etc.). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private MediaType type;

    /** Estado de consumo (VISTO, PENDIENTE, ABANDONADO). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status;

    /** Género de la obra (Ciencia Ficción, Terror, etc.). */
    private String genre;
    
    /** Creador principal (Autor, Director, Desarrollador). */
    private String creator;
    
    /** Puntuación personal (ej. 1-5). */
    private Integer rating;
    
    /** Fecha de lanzamiento de la obra original. */
    private LocalDate releaseDate;
    
    /** Fecha en que el usuario añadió el ítem a su colección. */
    private LocalDate dateAdded;

    /** Reseña o notas personales del usuario. */
    @Column(length = 2000)
    private String comment;

    /** Sinopsis o descripción de la obra proporcionada por la API externa. */
    @Column(length = 5000)
    private String description;

    /** Url de la imagen representativa del ítem (portada, póster, etc.). */
    @Column(name = "item_imageurl")
    private String itemImageUrl;

    /** Identificador del ítem en la API externa de origen (TMDB id, RAWG id, etc.). */
    private String externalId;

    /** Nombre de la fuente externa (TMDB, GOOGLE_BOOKS, JIKAN, RAWG). */
    private String externalSource;

    /** Nombre del álbum (solo para contenido musical). */
    private String album;

    /** Indica si el ítem fue creado manualmente por el usuario (no proviene de APIs externas). */
    @Column(nullable = false)
    private boolean custom = false;

    /** Usuario propietario de este ítem. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Etiquetas asociadas para clasificación personalizada. */
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(
        name = "media_tags",
        joinColumns = @JoinColumn(name = "media_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}