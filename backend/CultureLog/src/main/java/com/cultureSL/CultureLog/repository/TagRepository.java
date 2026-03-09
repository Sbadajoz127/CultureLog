package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * Busca una etiqueta por su nombre exacto.
     * Útil para reutilizar etiquetas existentes antes de crear una nueva.
     *
     * @param name Nombre de la etiqueta.
     * @return Un {@link Optional} con la etiqueta si existe.
     */
    Optional<Tag> findByName(String name);

    /**
     * Busca una etiqueta por nombre dentro del ámbito de un usuario específico.
     *
     * @param name   Nombre de la etiqueta.
     * @param userId ID del usuario propietario.
     * @return Un {@link Optional} con la etiqueta si existe para ese usuario.
     */
    Optional<Tag> findByNameAndUserId(String name, Long userId);
}