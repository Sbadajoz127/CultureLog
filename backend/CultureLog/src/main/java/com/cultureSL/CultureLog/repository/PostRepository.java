package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Post}.
 * <p>
 * Contiene la lógica central para la generación del Feed de noticias y la consulta
 * de perfiles de usuario.
 * </p>
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Genera el Feed de Noticias (Timeline) para un usuario.
     * <p>
     * Utiliza una consulta JPQL personalizada con {@code JOIN FETCH} para precargar
     * las relaciones del post (autor, ítem vinculado), evitando el problema N+1.
     * </p>
     * <p>
     * Selecciona publicaciones que cumplan TODAS las condiciones:
     * <ol>
     *   <li>El autor NO es el propio usuario solicitante.</li>
     *   <li>El autor es alguien a quien el usuario sigue (estado {@code ACCEPTED})
     *       O tiene el perfil configurado como {@code PUBLICO}.</li>
     *   <li>El usuario NO ha interactuado con el post (like, guardado o comentario).</li>
     * </ol>
     * </p>
     * Los resultados se ordenan cronológicamente descendente (lo más nuevo primero).
     *
     * @param userId   ID del usuario que visualiza el feed.
     * @param pageable configuración de paginación.
     * @return página de posts para el feed con relaciones precargadas.
     */
    @Query(value = "SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author a " +
           "LEFT JOIN FETCH p.linkedItem " +
           "LEFT JOIN a.settings s " +
           "WHERE p.author.id <> :userId " +
           "AND (" +
           "  (s IS NOT NULL AND s.profilePrivacy = 'PUBLICO') " +
           "  OR (s IS NOT NULL AND s.profilePrivacy = 'PRIVADO' " +
           "      AND p.author.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED')) " +
           "  OR (s IS NOT NULL AND s.profilePrivacy = 'SOLO_AMIGOS' " +
           "      AND p.author.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED') " +
           "      AND p.author.id IN (SELECT f2.follower.id FROM Follow f2 WHERE f2.followed.id = :userId AND f2.status = 'ACCEPTED')) " +
           "  OR (s IS NULL) " +
           ") " +
           "AND p.id NOT IN (SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :userId) " +
           "AND p.id NOT IN (SELECT ps.post.id FROM PostSave ps WHERE ps.user.id = :userId) " +
           "AND p.id NOT IN (SELECT c.post.id FROM Comment c WHERE c.author.id = :userId)",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Post p " +
           "LEFT JOIN p.author a " +
           "LEFT JOIN a.settings s " +
           "WHERE p.author.id <> :userId " +
           "AND (" +
           "  (s IS NOT NULL AND s.profilePrivacy = 'PUBLICO') " +
           "  OR (s IS NOT NULL AND s.profilePrivacy = 'PRIVADO' " +
           "      AND p.author.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED')) " +
           "  OR (s IS NOT NULL AND s.profilePrivacy = 'SOLO_AMIGOS' " +
           "      AND p.author.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED') " +
           "      AND p.author.id IN (SELECT f2.follower.id FROM Follow f2 WHERE f2.followed.id = :userId AND f2.status = 'ACCEPTED')) " +
           "  OR (s IS NULL) " +
           ") " +
           "AND p.id NOT IN (SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :userId) " +
           "AND p.id NOT IN (SELECT ps.post.id FROM PostSave ps WHERE ps.user.id = :userId) " +
           "AND p.id NOT IN (SELECT c.post.id FROM Comment c WHERE c.author.id = :userId)")
    Page<Post> findNewsFeed(@Param("userId") Long userId, Pageable pageable);

    /**
     * Recupera todas las publicaciones creadas por un usuario específico (vista de Perfil),
     * con JOIN FETCH para evitar N+1 en author y linkedItem.
     *
     * @param userId   ID del autor de los posts.
     * @param pageable configuración de paginación.
     * @return página de posts pertenecientes al usuario.
     */
    @Query(value = "SELECT p FROM Post p LEFT JOIN FETCH p.author LEFT JOIN FETCH p.linkedItem " +
           "WHERE p.author.id = :userId ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p WHERE p.author.id = :userId")
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Incrementa o decrementa atómicamente el contador de likes de un post.
     * <p>
     * Usa {@code clearAutomatically = true} para invalidar el contexto de persistencia
     * tras la actualización, garantizando que lecturas posteriores reflejen el nuevo valor.
     * </p>
     *
     * @param postId ID del post.
     * @param delta  valor a sumar (1 para like, -1 para unlike).
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = CASE WHEN p.likeCount + :delta < 0 THEN 0 ELSE p.likeCount + :delta END WHERE p.id = :postId")
    void updateLikeCount(@Param("postId") Long postId, @Param("delta") int delta);

    /**
     * Incrementa o decrementa atómicamente el contador de comentarios de un post.
     * <p>
     * Usa {@code clearAutomatically = true} para invalidar el contexto de persistencia
     * tras la actualización, garantizando que lecturas posteriores reflejen el nuevo valor.
     * </p>
     *
     * @param postId ID del post.
     * @param delta  valor a sumar (1 para nuevo comentario, -1 para eliminación).
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = CASE WHEN p.commentCount + :delta < 0 THEN 0 ELSE p.commentCount + :delta END WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") Long postId, @Param("delta") int delta);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE posts p SET p.comment_count = (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id)", nativeQuery = true)
    int syncAllCommentCounts();

    /**
     * Quita la referencia al ítem multimedia antes de borrarlo (evita violación de FK en media_item_id).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.linkedItem = null WHERE p.linkedItem.id = :mediaItemId")
    void unlinkMediaItem(@Param("mediaItemId") Long mediaItemId);

    long countByAuthorId(Long authorId);

    @Query("SELECT p.author.username, COUNT(p) FROM Post p GROUP BY p.author.username ORDER BY COUNT(p) DESC")
    List<Object[]> countPostsGroupedByUser(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.linkedItem",
           countQuery = "SELECT COUNT(p) FROM Post p")
    Page<Post> findAllWithAuthorAndItem(Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.linkedItem " +
           "WHERE (:authorId IS NULL OR p.author.id = :authorId) " +
           "AND (:linkedItemId IS NULL OR p.linkedItem.id = :linkedItemId)",
           countQuery = "SELECT COUNT(p) FROM Post p " +
           "WHERE (:authorId IS NULL OR p.author.id = :authorId) " +
           "AND (:linkedItemId IS NULL OR p.linkedItem.id = :linkedItemId)")
    Page<Post> findAllWithFilters(@Param("authorId") Long authorId,
                                   @Param("linkedItemId") Long linkedItemId,
                                   Pageable pageable);

    @Query("SELECT DISTINCT p.linkedItem FROM Post p WHERE p.linkedItem IS NOT NULL " +
           "AND (:q IS NULL OR :q = '' OR LOWER(p.linkedItem.title) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<MediaItem> findDistinctLinkedItems(@Param("q") String q);

    /**
     * Obtiene los posts que un usuario ha dado like, con JOIN FETCH para evitar N+1.
     */
    @Query(value = "SELECT p FROM Post p JOIN PostLike pl ON pl.post = p " +
           "LEFT JOIN FETCH p.author LEFT JOIN FETCH p.linkedItem " +
           "WHERE pl.user.id = :userId ORDER BY pl.likedAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p JOIN PostLike pl ON pl.post = p WHERE pl.user.id = :userId")
    Page<Post> findLikedPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Obtiene los posts guardados por un usuario, con JOIN FETCH para evitar N+1.
     */
    @Query(value = "SELECT p FROM Post p JOIN PostSave ps ON ps.post = p " +
           "LEFT JOIN FETCH p.author LEFT JOIN FETCH p.linkedItem " +
           "WHERE ps.user.id = :userId ORDER BY ps.savedAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p JOIN PostSave ps ON ps.post = p WHERE ps.user.id = :userId")
    Page<Post> findSavedPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Busca publicaciones por título del ítem vinculado o contenido del post.
     * Solo devuelve posts visibles para el usuario (propios o de usuarios seguidos/públicos).
     */
    @Query(value = "SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author a " +
           "LEFT JOIN FETCH p.linkedItem " +
           "LEFT JOIN a.settings s " +
           "WHERE (LOWER(p.linkedItemTitle) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "       OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (p.author.id = :userId " +
           "     OR (s IS NULL OR s.profilePrivacy = 'PUBLICO') " +
           "     OR (s IS NOT NULL AND s.profilePrivacy = 'PRIVADO' " +
           "         AND p.author.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED')) " +
           "     OR (s IS NOT NULL AND s.profilePrivacy = 'SOLO_AMIGOS' " +
           "         AND p.author.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED') " +
           "         AND p.author.id IN (SELECT f2.follower.id FROM Follow f2 WHERE f2.followed.id = :userId AND f2.status = 'ACCEPTED')))",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Post p " +
           "LEFT JOIN p.author a " +
           "LEFT JOIN a.settings s " +
           "WHERE (LOWER(p.linkedItemTitle) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "       OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (p.author.id = :userId " +
           "     OR (s IS NULL OR s.profilePrivacy = 'PUBLICO') " +
           "     OR (s IS NOT NULL AND s.profilePrivacy = 'PRIVADO' " +
           "         AND p.author.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED')) " +
           "     OR (s IS NOT NULL AND s.profilePrivacy = 'SOLO_AMIGOS' " +
           "         AND p.author.id IN (SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED') " +
           "         AND p.author.id IN (SELECT f2.follower.id FROM Follow f2 WHERE f2.followed.id = :userId AND f2.status = 'ACCEPTED')))")
    Page<Post> searchPosts(@Param("query") String query, @Param("userId") Long userId, Pageable pageable);
}
