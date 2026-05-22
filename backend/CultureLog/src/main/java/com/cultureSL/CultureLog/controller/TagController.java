package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.TagRequest;
import com.cultureSL.CultureLog.dto.TagResponse;
import com.cultureSL.CultureLog.mapper.TagMapper;
import com.cultureSL.CultureLog.model.Tag;
import com.cultureSL.CultureLog.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de etiquetas del usuario.
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    @GetMapping
    public ResponseEntity<List<TagResponse>> getUserTags(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<Tag> tags = tagService.getUserTags(userId);
        return ResponseEntity.ok(tagMapper.toDtoList(tags));
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(
            Authentication authentication,
            @Valid @RequestBody TagRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        Tag tag = tagService.createTag(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tagMapper.toDto(tag));
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long tagId,
            Authentication authentication,
            @Valid @RequestBody TagRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        Tag tag = tagService.updateTag(tagId, userId, request);
        return ResponseEntity.ok(tagMapper.toDto(tag));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long tagId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        tagService.deleteTag(tagId, userId);
        return ResponseEntity.noContent().build();
    }
}
