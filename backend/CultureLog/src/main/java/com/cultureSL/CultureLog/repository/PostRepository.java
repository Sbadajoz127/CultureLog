package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Utiliza una consulta JPQL personalizada para seleccionar publicaciones que cumplan
     * una de las siguientes condiciones:
     * 1. El autor es el propio usuario solicitante.
     * 2. El autor es alguien a quien el usuario sigue y cuya solicitud de seguimiento ha sido {@code ACCEPTED}.
     * </p>
     * Los resultados se ordenan cronol?gicamente descendente (lo m?s nuevo primero).
     *
     * @param userId   ID del usuario que visualiza el feed.
     * @param pageable Configuraci?n de paginaci?n.
     * @return P?gina de posts para el feed.
     */
    @Query("SELECT p FROM Post p WHERE p.author.id = :userId OR p.author.id IN " +
           "(SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED') " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findNewsFeed(@Param("userId") Long userId, Pageable pageable);

    /**
     * Recupera todas las publicaciones creadas por un usuario espec?fico (vista de Perfil).
     * Ordenadas de m?s reciente a m?s antigua.
     *
     * @param userId   ID del autor de los posts.
     * @param pageable Configuraci?n de paginaci?n.
     * @return P?gina de posts pertenecientes al usuario.
     */
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}