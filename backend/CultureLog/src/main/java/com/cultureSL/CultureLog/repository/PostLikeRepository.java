package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

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

    /**
     * Obtiene los IDs de posts a los que un usuario ha dado like dentro de un conjunto dado.
     * <p>
     * Optimización para el feed: en lugar de consultar uno a uno, resuelve en bloque
     * qué posts del feed actual tienen like del usuario.
     * </p>
     *
     * @param userId  ID del usuario.
     * @param postIds colección de IDs de posts a verificar.
     * @return conjunto de IDs de posts a los que el usuario dio like.
     */
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :userId AND pl.post.id IN :postIds")
    Set<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);
}