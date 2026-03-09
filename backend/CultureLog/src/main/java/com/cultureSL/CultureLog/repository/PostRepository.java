package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Post}.
 * <p>
 * Contiene la l?gica central para la generaci?n del Feed de noticias y la consulta
 * de perfiles de usuario.
 * </p>
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Genera el Feed de Noticias (Timeline) para un usuario.
     * <p>
     * Utiliza una consulta JPQL personalizada con {@code JOIN FETCH} para precargar
     * las relaciones del post (autor, ?tem vinculado, comentarios y autores de comentarios),
     * evitando el problema N+1 y posibles {@code LazyInitializationException}.
     * </p>
     * <p>
     * Selecciona publicaciones que cumplan una de las siguientes condiciones:
     * <ol>
     *   <li>El autor es el propio usuario solicitante.</li>
     *   <li>El autor es alguien a quien el usuario sigue con estado {@code ACCEPTED}.</li>
     * </ol>
     * </p>
     * Los resultados se ordenan cronol?gicamente descendente (lo m?s nuevo primero).
     * Se incluye una {@code countQuery} separada para compatibilidad con la paginaci?n.
     *
     * @param userId   ID del usuario que visualiza el feed.
     * @param pageable configuraci?n de paginaci?n.
     * @return p?gina de posts para el feed con relaciones precargadas.
     */
    @Query(value = "SELECT DISTINCT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.linkedItem " +
           "WHERE p.author.id = :userId OR p.author.id IN " +
           "(SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED')",
           countQuery = "SELECT COUNT(DISTINCT p) FROM Post p WHERE p.author.id = :userId OR p.author.id IN " +
           "(SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED')")
    Page<Post> findNewsFeed(@Param("userId") Long userId, Pageable pageable);

    /**
     * Recupera todas las publicaciones creadas por un usuario espec?fico (vista de Perfil).
     * Ordenadas de m?s reciente a m?s antigua.
     *
     * @param userId   ID del autor de los posts.
     * @param pageable configuraci?n de paginaci?n.
     * @return p?gina de posts pertenecientes al usuario.
     */
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Incrementa o decrementa at?micamente el contador de likes de un post.
     * <p>
     * Usa {@code clearAutomatically = true} para invalidar el contexto de persistencia
     * tras la actualizaci?n, garantizando que lecturas posteriores reflejen el nuevo valor.
     * </p>
     *
     * @param postId ID del post.
     * @param delta  valor a sumar (1 para like, -1 para unlike).
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = CASE WHEN p.likeCount + :delta < 0 THEN 0 ELSE p.likeCount + :delta END WHERE p.id = :postId")
    void updateLikeCount(@Param("postId") Long postId, @Param("delta") int delta);

    /**
     * Incrementa o decrementa at?micamente el contador de comentarios de un post.
     * <p>
     * Usa {@code clearAutomatically = true} para invalidar el contexto de persistencia
     * tras la actualizaci?n, garantizando que lecturas posteriores reflejen el nuevo valor.
     * </p>
     *
     * @param postId ID del post.
     * @param delta  valor a sumar (1 para nuevo comentario, -1 para eliminaci?n).
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = CASE WHEN p.commentCount + :delta < 0 THEN 0 ELSE p.commentCount + :delta END WHERE p.id = :postId")
    void updateCommentCount(@Param("postId") Long postId, @Param("delta") int delta);
}