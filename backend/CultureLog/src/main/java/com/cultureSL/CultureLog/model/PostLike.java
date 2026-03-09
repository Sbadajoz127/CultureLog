package com.cultureSL.CultureLog.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que registra la acción de "Me gusta" de un usuario sobre un post.
 * <p>
 * Utiliza una clave única compuesta (post_id + user_id) para asegurar que un usuario
 * solo pueda dar like una vez a la misma publicación.
 * </p>
 */
@Entity
@Table(name = "post_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PostLike {

    /** Identificador único del like. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Publicación que recibió el like. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** Usuario que dio el like. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Fecha y hora en que se registró el like. */
    @CreationTimestamp
    private LocalDateTime likedAt;

    /**
     * Crea un nuevo like vinculando un post con un usuario.
     *
     * @param post publicación que recibe el like
     * @param user usuario que da el like
     */
    public PostLike(Post post, User user) {
        this.post = post;
        this.user = user;
    }
}