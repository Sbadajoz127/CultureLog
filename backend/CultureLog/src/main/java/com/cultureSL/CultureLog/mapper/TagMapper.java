package com.cultureSL.CultureLog.mapper;

import com.cultureSL.CultureLog.dto.TagResponse;
import com.cultureSL.CultureLog.model.Tag;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class TagMapper {

    public TagResponse toDto(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .colorHex(tag.getColor().getHexCode())
                .build();
    }

    public List<TagResponse> toDtoList(Collection<Tag> tags) {
        return tags.stream().map(this::toDto).toList();
    }
}
