package com.cultureSL.CultureLog.mapper;

import com.cultureSL.CultureLog.dto.MediaItemResponse;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.Tag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente encargado de convertir entidades {@link MediaItem} en DTOs {@link MediaItemResponse}.
 * <p>
 * Transforma los datos de la entidad JPA, incluyendo los nombres de las etiquetas
 * asociadas, en un formato adecuado para la respuesta de la API REST.
 * </p>
 */
@Component
public class MediaItemMapper {

    /**
     * Convierte un ítem multimedia en su DTO de respuesta.
     *
     * @param item entidad del ítem a convertir
     * @return DTO con los datos del ítem y los nombres de sus etiquetas
     */
    public MediaItemResponse toDto(MediaItem item) {
        return MediaItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .itemImageUrl(item.getItemImageUrl())
                .type(item.getType())
                .status(item.getStatus())
                .genre(item.getGenre())
                .creator(item.getCreator())
                .rating(item.getRating())
                .releaseDate(item.getReleaseDate())
                .dateAdded(item.getDateAdded())
                .comment(item.getComment())
                .tagNames(item.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet()))
                .build();
    }

    /**
     * Convierte una lista de ítems multimedia en DTOs de respuesta.
     *
     * @param items lista de entidades a convertir
     * @return lista de DTOs
     */
    public List<MediaItemResponse> toDtoList(List<MediaItem> items) {
        return items.stream().map(this::toDto).toList();
    }
}
