package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.repository.MediaItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Detecta si un usuario ya tiene un ítem equivalente en biblioteca (misma clave externa o mismo título+tipo).
 */
@Component
@RequiredArgsConstructor
public class MediaLibraryDuplicateFinder {

    private final MediaItemRepository mediaItemRepository;

    /**
     * Prioridad: (userId + externalId + externalSource + type) si los identificadores
     * externos vienen informados; si no, (userId + tipo + título ignorando mayúsculas).
     * <p>
     * Se incluye {@code type} en la rama externa porque Jikan usa el mismo
     * {@code source} para anime y manga, y los {@code mal_id} son independientes.
     */
    public Optional<MediaItem> findDuplicate(
            Long userId,
            MediaType type,
            String title,
            String externalId,
            String externalSource) {

        if (externalId != null && !externalId.isBlank()
                && externalSource != null && !externalSource.isBlank()) {
            return mediaItemRepository.findFirstByUserIdAndExternalIdAndExternalSourceAndType(
                    userId, externalId.trim(), externalSource.trim(), type);
        }
        if (type != null && title != null && !title.isBlank()) {
            return mediaItemRepository.findFirstByUserIdAndTypeAndTitleIgnoreCase(
                    userId, type, title.trim());
        }
        return Optional.empty();
    }
}
