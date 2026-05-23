package com.cultureSL.CultureLog.mapper;

import com.cultureSL.CultureLog.dto.MediaItemResponse;
import com.cultureSL.CultureLog.model.MediaItem;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Componente encargado de convertir entidades {@link MediaItem} en DTOs {@link MediaItemResponse}.
 */
@Component
@RequiredArgsConstructor
public class MediaItemMapper {

    private final TagMapper tagMapper;

    public MediaItemResponse toDto(MediaItem item) {
        boolean tagsLoaded = Hibernate.isInitialized(item.getTags());
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
                .description(item.getDescription())
                .externalId(item.getExternalId())
                .externalSource(item.getExternalSource())
                .album(item.getAlbum())
                .custom(item.isCustom())
                .tags(tagsLoaded ? tagMapper.toDtoList(item.getTags()) : Collections.emptyList())
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
