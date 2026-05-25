package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.PostSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repositorio para gestionar los posts guardados por usuarios.
 */
@Repository
public interface PostSaveRepository extends JpaRepository<PostSave, Long> {

    /**
     * Busca si un usuario ha guardado un post específico.
     */
    Optional<PostSave> findByPostIdAndUserId(Long postId, Long userId);

    /**
     * Verifica si existe un guardado para un post y usuario.
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /**
     * Cuenta los posts guardados por un usuario.
     */
    long countByUserId(Long userId);

    /**
     * Elimina todos los guardados de un post específico.
     */
    void deleteByPostId(Long postId);

    /**
     * Obtiene los IDs de los posts guardados por un usuario de una lista dada.
     */
    @Query("SELECT ps.post.id FROM PostSave ps WHERE ps.user.id = :userId AND ps.post.id IN :postIds")
    Set<Long> findSavedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}
