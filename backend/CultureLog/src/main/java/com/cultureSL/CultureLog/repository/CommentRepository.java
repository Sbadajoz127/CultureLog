package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link Comment}.
 * <p>
 * Proporciona métodos para recuperar los comentarios asociados a las publicaciones.
 * </p>
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Recupera todos los comentarios asociados a un post específico.
     * <p>
     * Los resultados se ordenan cronológicamente de forma ascendente (del más antiguo al más nuevo),
     * simulando el hilo de una conversación natural.
     * </p>
     *
     * @param postId ID de la publicación (Post).
     * @return Lista de comentarios ordenados por fecha de creación ascendente.
     */
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    long countByPostId(Long postId);
}