package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "media_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type; // LIBRO, PELICULA...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaStatus status; // VISTO, POR_VER...

    private String genre;   // Ej: "Ciencia Ficción"
    private String creator; // Autor, Director o Desarrollador
    private Integer rating; // 1 a 5 (o 1 a 10)
    
    private LocalDate releaseDate; // Fecha de lanzamiento de la obra
    private LocalDate dateAdded;   // Fecha en que tú la registraste (se puede poner auto)

    @Column(length = 2000)
    private String comment; // Tu reseña personal

    // --- RELACIONES ---

    // Muchos items pertenecen a Un usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación Muchos a Muchos para las etiquetas
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "media_tags",
        joinColumns = @JoinColumn(name = "media_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // --- PERSONALIZACIÓN TOTAL ---
    
    // Esto crea una tabla extra para guardar pares Clave-Valor dinámicos
    // Ej: <"Páginas", "350"> para libros, o <"Plataforma", "Steam"> para juegos
    @ElementCollection
    @CollectionTable(name = "media_custom_attributes", 
                     joinColumns = @JoinColumn(name = "media_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> customAttributes = new HashMap<>();
    
    // Método helper para añadir atributos fácilmente
    public void addAttribute(String key, String value) {
        this.customAttributes.put(key, value);
    }
}