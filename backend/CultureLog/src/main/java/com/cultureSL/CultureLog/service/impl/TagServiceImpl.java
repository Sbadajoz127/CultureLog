package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.TagRequest;
import com.cultureSL.CultureLog.exception.DuplicateItemException;
import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
import com.cultureSL.CultureLog.exception.UnauthorizedException;
import com.cultureSL.CultureLog.model.Tag;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.repository.TagRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Tag> getUserTags(Long userId) {
        return tagRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Tag createTag(Long userId, TagRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String trimmedName = request.getName().trim();
        if (tagRepository.existsByNameIgnoreCaseAndUserId(trimmedName, userId)) {
            throw new DuplicateItemException("Ya tienes una etiqueta con ese nombre");
        }

        Tag tag = new Tag();
        tag.setName(trimmedName);
        tag.setColor(request.getColor() != null ? request.getColor() : com.cultureSL.CultureLog.model.enums.TagColor.POR_DEFECTO);
        tag.setUser(user);

        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public Tag updateTag(Long tagId, Long userId, TagRequest request) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada"));

        if (!tag.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para editar esta etiqueta");
        }

        String trimmedName = request.getName().trim();
        tagRepository.findByNameIgnoreCaseAndUserId(trimmedName, userId).ifPresent(existing -> {
            if (!existing.getId().equals(tagId)) {
                throw new DuplicateItemException("Ya tienes una etiqueta con ese nombre");
            }
        });

        tag.setName(trimmedName);
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }

        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId, Long userId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada"));

        if (!tag.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar esta etiqueta");
        }

        tagRepository.removeAllMediaTagAssociations(tagId);
        tagRepository.delete(tag);
    }
}
