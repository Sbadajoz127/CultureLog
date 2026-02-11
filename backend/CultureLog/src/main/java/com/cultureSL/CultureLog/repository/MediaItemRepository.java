package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link MediaItem}.
 * <p>
 * Permite realizar búsquedas avanzadas sobre la biblioteca del usuario, incluyendo
 * filtros por tipo, estado, género y búsquedas por texto o etiquetas.
 * </p>
 */
@Repository
public interface MediaItemRepository extends JpaRepository<MediaItem, Long> {

    /**
     * Recupera toda la biblioteca de un usuario sin filtros adicionales.
     *
     * @param userId ID del usuario propietario.
     * @return Lista completa de items multimedia del usuario.
     */
    List<MediaItem> findByUserId(Long userId);

    /**
     * Filtra la biblioteca del usuario por tipo de medio.
     * Ej: "Ver solo mis LIBROS".
     *
     * @param userId ID del usuario.
     * @param type   Tipo de medio (ej. LIBRO, PELICULA).
     * @return Lista de items que coinciden con el tipo.
     */
    List<MediaItem> findByUserIdAndType(Long userId, MediaType type);

    /**
     * Filtra la biblioteca del usuario por estado de consumo.
     * Ej: "Ver solo lo que tengo POR_VER".
     *
     * @param userId ID del usuario.
     * @param status Estado del ítem (ej. VISTO, POR_VER).
     * @return Lista de items que coinciden con el estado.
     */
    List<MediaItem> findByUserIdAndStatus(Long userId, MediaStatus status);
    
    /**
     * Filtra la biblioteca del usuario por género.
     *
     * @param userId ID del usuario.
     * @param genre  Nombre del género (ej. "Ciencia Ficción").
     * @return Lista de items de ese género.
     */
    List<MediaItem> findByUserIdAndGenre(Long userId, String genre);

    /**
     * Realiza una búsqueda por título dentro de la biblioteca del usuario.
     * La búsqueda no distingue entre mayúsculas y minúsculas (IgnoreCase).
     *
     * @param userId ID del usuario.
     * @param title  Fragmento del título a buscar.
     * @return Lista de items cuyo título contiene el texto proporcionado.
     */
    List<MediaItem> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
    
    /**
     * Busca items que tengan asignada una etiqueta específica.
     *
     * @param userId  ID del usuario.
     * @param tagName Nombre de la etiqueta (Tag).
     * @return Lista de items etiquetados con ese nombre.
     */
    List<MediaItem> findByUserIdAndTags_Name(Long userId, String tagName);
}