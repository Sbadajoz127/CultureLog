package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.dto.MediaItemRequest;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;

import java.util.List;

public interface MediaItemService {
    
    List<MediaItem> getUserItems(Long userId);
    
    List<MediaItem> filterItems(Long userId, MediaType type, MediaStatus status);

    MediaItem addItem(Long userId, MediaItemRequest request);

    MediaItem updateItem(Long itemId, Long userId, MediaItemRequest request);

    void deleteItem(Long itemId, Long userId);
}