package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.MediaStatus;
import com.cultureSL.CultureLog.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaItemRepository extends JpaRepository<MediaItem, Long> {

    // 1. Obtener toda la biblioteca de un usuario específico
    List<MediaItem> findByUserId(Long userId);

    // 2. Filtrar por TIPO (Ej: Ver solo "LIBRO"s de un usuario)
    List<MediaItem> findByUserIdAndType(Long userId, MediaType type);

    // 3. Filtrar por ESTADO (Ej: Ver todo lo "POR_VER")
    List<MediaItem> findByUserIdAndStatus(Long userId, MediaStatus status);
    
    // 4. Filtrar por GÉNERO (Ej: Ver todo lo de "Ciencia Ficción")
    List<MediaItem> findByUserIdAndGenre(Long userId, String genre);

    // 5. Buscador: Buscar por título (ignorando mayúsculas/minúsculas)
    // SQL equivalente: WHERE user_id = ? AND LOWER(title) LIKE %?%
    List<MediaItem> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
    
    // 6. Filtrar por una Etiqueta específica (Avanzado)
    // Busca items del usuario que tengan una etiqueta con cierto nombre
    List<MediaItem> findByUserIdAndTags_Name(Long userId, String tagName);
}