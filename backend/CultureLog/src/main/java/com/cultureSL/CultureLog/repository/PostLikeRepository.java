package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link PostLike}.
 * <p>
 * Gestiona las interacciones de "Me gusta" en las publicaciones, asegurando
 * que cada usuario solo pueda dar un like por post.
 * </p>
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * Verifica si un usuario ya ha dado "Me gusta" a una publicación específica.
     * Utilizado para determinar el estado visual del botón de like (rojo/gris).
     *
     * @param postId ID de la publicación.
     * @param userId ID del usuario.
     * @return {@code true} si el like existe.
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * Recupera la entidad de Like específica entre un usuario y un post.
     * Necesario para poder eliminar el like (acción de Dislike).
     *
     * @param postId ID de la publicación.
     * @param userId ID del usuario.
     * @return Un {@link Optional} con la entidad PostLike.
     */
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    
    /**
     * Cuenta el número total de likes que tiene una publicación.
     *
     * @param postId ID de la publicación.
     * @return Cantidad total de likes.
     */
    long countByPostId(Long postId);
}