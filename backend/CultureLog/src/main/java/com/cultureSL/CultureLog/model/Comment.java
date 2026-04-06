package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un comentario realizado por un usuario en una publicación específica.
 * <p>
 * Esta entidad permite la interacción social mediante texto, vinculando a un autor
 * con el post original. Los comentarios se ordenan cronológicamente.
 * </p>
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Comment {

    /** Identificador único del comentario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Contenido textual del comentario.
     * Limitado a 1000 caracteres para controlar la longitud en la base de datos.
     */
    @Column(nullable = false, length = 1000)
    private String text;

    /** Fecha y hora exacta en la que se creó el comentario. */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Usuario que escribió el comentario.
     * Relación Muchos-a-Uno (Lazy Loading).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    /**
     * Publicación a la que pertenece este comentario.
     * Relación Muchos-a-Uno (Lazy Loading).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * Comentario padre si este comentario es una respuesta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    /**
     * Respuestas a este comentario.
     */
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();
}