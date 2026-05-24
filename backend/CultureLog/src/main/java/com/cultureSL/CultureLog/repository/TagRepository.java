package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link Tag}.
 * <p>
 * Permite la gestión y búsqueda de etiquetas utilizadas para clasificar items multimedia.
 * </p>
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * Busca una etiqueta por nombre dentro del ámbito de un usuario específico (case-insensitive).
     *
     * @param name   Nombre de la etiqueta.
     * @param userId ID del usuario propietario.
     * @return Un {@link Optional} con la etiqueta si existe para ese usuario.
     */
    Optional<Tag> findByNameIgnoreCaseAndUserId(String name, Long userId);

    List<Tag> findByUserId(Long userId);

    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

    @Modifying
    @Query(value = "DELETE FROM media_tags WHERE tag_id = :tagId", nativeQuery = true)
    void removeAllMediaTagAssociations(@Param("tagId") Long tagId);
}