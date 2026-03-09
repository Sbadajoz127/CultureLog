package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una publicación en el Feed social de la aplicación.
 * <p>
 * Un post puede consistir solo en texto o estar vinculado a un {@link MediaItem} de la biblioteca
 * del usuario (ej: una reseña de una película). Agrupa comentarios y likes.
 * </p>
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post {

    /** Identificador único del post. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Contenido textual del post. */
    @Column(length = 2000)
    private String content;

    /** Fecha y hora de creación del post (generada automáticamente). */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /** Autor de la publicación. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    /**
     * (Opcional) Ítem multimedia sobre el que habla el post.
     * Permite mostrar una "tarjeta" del libro/película dentro del post.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_item_id")
    private MediaItem linkedItem;

    /** Lista de comentarios recibidos. */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    /** Lista de interacciones "Me gusta". */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    /** Contador desnormalizado de likes para optimizar lectura. */
    private int likeCount = 0;
    
    /** Contador desnormalizado de comentarios para optimizar lectura. */
    private int commentCount = 0;
}