package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.dto.TagRequest;
import com.cultureSL.CultureLog.model.Tag;

import java.util.List;

/**
 * Servicio para la gestión de etiquetas personales del usuario.
 */
public interface TagService {

    List<Tag> getUserTags(Long userId);

    Tag createTag(Long userId, TagRequest request);

    Tag updateTag(Long tagId, Long userId, TagRequest request);

    void deleteTag(Long tagId, Long userId);
}
