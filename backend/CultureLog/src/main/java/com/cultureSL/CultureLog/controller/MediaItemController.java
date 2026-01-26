package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.MediaItemRequest;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.service.MediaItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class MediaItemController {

    private final MediaItemService mediaItemService;

    @GetMapping
    public ResponseEntity<List<MediaItem>> getUserItems(
            @RequestParam Long userId,
            @RequestParam(required = false) MediaType type,
            @RequestParam(required = false) MediaStatus status) {
        
        if (type == null && status == null) {
            return ResponseEntity.ok(mediaItemService.getUserItems(userId));
        } else {
            return ResponseEntity.ok(mediaItemService.filterItems(userId, type, status));
        }
    }

    @PostMapping
    public ResponseEntity<MediaItem> addItem(
            @RequestParam Long userId, 
            @RequestBody MediaItemRequest request) {
        return ResponseEntity.ok(mediaItemService.addItem(userId, request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<MediaItem> updateItem(
            @PathVariable Long itemId,
            @RequestParam Long userId,
            @RequestBody MediaItemRequest request) {
        return ResponseEntity.ok(mediaItemService.updateItem(itemId, userId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long itemId,
            @RequestParam Long userId) {
        mediaItemService.deleteItem(itemId, userId);
        return ResponseEntity.ok().build();
    }
}