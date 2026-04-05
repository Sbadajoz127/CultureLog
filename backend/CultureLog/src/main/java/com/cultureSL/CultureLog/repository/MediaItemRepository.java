package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
     * Recupera toda la biblioteca de un usuario con sus etiquetas precargadas.
     *
     * @param userId ID del usuario propietario.
     * @return Lista completa de items multimedia del usuario.
     */
    @Query("SELECT DISTINCT m FROM MediaItem m LEFT JOIN FETCH m.tags WHERE m.user.id = :userId")
    List<MediaItem> findByUserIdWithTags(@Param("userId") Long userId);

    /**
     * Recupera toda la biblioteca de un usuario sin filtros adicionales.
     *
     * @param userId ID del usuario propietario.
     * @return Lista completa de items multimedia del usuario.
     */
    List<MediaItem> findByUserId(Long userId);

    /**
     * Filtra la biblioteca del usuario por tipo de medio, con etiquetas precargadas.
     *
     * @param userId ID del usuario.
     * @param type   Tipo de medio (ej. LIBRO, PELICULA).
     * @return Lista de items que coinciden con el tipo.
     */
    @Query("SELECT DISTINCT m FROM MediaItem m LEFT JOIN FETCH m.tags WHERE m.user.id = :userId AND m.type = :type")
    List<MediaItem> findByUserIdAndTypeWithTags(@Param("userId") Long userId, @Param("type") MediaType type);

    /**
     * Filtra la biblioteca del usuario por tipo de medio.
     *
     * @param userId ID del usuario.
     * @param type   Tipo de medio (ej. LIBRO, PELICULA).
     * @return Lista de items que coinciden con el tipo.
     */
    List<MediaItem> findByUserIdAndType(Long userId, MediaType type);

    /**
     * Filtra la biblioteca del usuario por estado de consumo, con etiquetas precargadas.
     *
     * @param userId ID del usuario.
     * @param status Estado del ítem (ej. VISTO, POR_VER).
     * @return Lista de items que coinciden con el estado.
     */
    @Query("SELECT DISTINCT m FROM MediaItem m LEFT JOIN FETCH m.tags WHERE m.user.id = :userId AND m.status = :status")
    List<MediaItem> findByUserIdAndStatusWithTags(@Param("userId") Long userId, @Param("status") MediaStatus status);

    /**
     * Filtra la biblioteca del usuario por estado de consumo.
     *
     * @param userId ID del usuario.
     * @param status Estado del ítem (ej. VISTO, POR_VER).
     * @return Lista de items que coinciden con el estado.
     */
    List<MediaItem> findByUserIdAndStatus(Long userId, MediaStatus status);

    /**
     * Filtra la biblioteca del usuario combinando tipo de medio y estado de consumo, con etiquetas precargadas.
     *
     * @param userId ID del usuario.
     * @param type   tipo de medio (ej. LIBRO, PELICULA).
     * @param status estado del ítem (ej. VISTO, POR_VER).
     * @return lista de items que coinciden con ambos criterios.
     */
    @Query("SELECT DISTINCT m FROM MediaItem m LEFT JOIN FETCH m.tags WHERE m.user.id = :userId AND m.type = :type AND m.status = :status")
    List<MediaItem> findByUserIdAndTypeAndStatusWithTags(@Param("userId") Long userId, @Param("type") MediaType type, @Param("status") MediaStatus status);

    /**
     * Filtra la biblioteca del usuario combinando tipo de medio y estado de consumo.
     *
     * @param userId ID del usuario.
     * @param type   tipo de medio (ej. LIBRO, PELICULA).
     * @param status estado del ítem (ej. VISTO, POR_VER).
     * @return lista de items que coinciden con ambos criterios.
     */
    List<MediaItem> findByUserIdAndTypeAndStatus(Long userId, MediaType type, MediaStatus status);

    /**
     * Recupera un item por su ID con etiquetas precargadas.
     *
     * @param itemId ID del item.
     * @return Optional con el item y sus tags.
     */
    @Query("SELECT m FROM MediaItem m LEFT JOIN FETCH m.tags WHERE m.id = :itemId")
    Optional<MediaItem> findByIdWithTags(@Param("itemId") Long itemId);

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

    /**
     * Comprueba si ya existe un ítem de una API externa en la biblioteca de un usuario.
     *
     * @param userId         ID del usuario.
     * @param externalId     identificador del recurso en la API de origen.
     * @param externalSource nombre de la fuente externa (TMDB, JIKAN, etc.).
     * @return {@code true} si el ítem ya existe en la biblioteca del usuario
     */
    boolean existsByUserIdAndExternalIdAndExternalSource(Long userId, String externalId, String externalSource);

    Optional<MediaItem> findFirstByUserIdAndExternalIdAndExternalSourceAndType(
            Long userId, String externalId, String externalSource, MediaType type);

    Optional<MediaItem> findFirstByUserIdAndTypeAndTitleIgnoreCase(
            Long userId, MediaType type, String title);
}