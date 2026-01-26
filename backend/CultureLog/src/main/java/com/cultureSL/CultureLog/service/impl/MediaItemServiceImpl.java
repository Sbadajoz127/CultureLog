package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.MediaItemRequest;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.repository.MediaItemRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.MediaItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaItemServiceImpl implements MediaItemService {

    private final MediaItemRepository mediaItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MediaItem> getUserItems(Long userId) {
        return mediaItemRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MediaItem> filterItems(Long userId, MediaType type, MediaStatus status) {
        if (type != null && status != null) {
            return mediaItemRepository.findByUserIdAndType(userId, type).stream()
                    .filter(i -> i.getStatus() == status)
                    .toList();
        } else if (type != null) {
            return mediaItemRepository.findByUserIdAndType(userId, type);
        } else if (status != null) {
            return mediaItemRepository.findByUserIdAndStatus(userId, status);
        }
        return getUserItems(userId);
    }

    @Override
    @Transactional
    public MediaItem addItem(Long userId, MediaItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        MediaItem item = new MediaItem();
        item.setTitle(request.getTitle());
        item.setType(request.getType());
        item.setStatus(request.getStatus());
        item.setGenre(request.getGenre());
        item.setRating(request.getRating());
        item.setComment(request.getComment());
        item.setDateAdded(LocalDate.now());
        item.setUser(user);

        return mediaItemRepository.save(item);
    }

    @Override
    @Transactional
    public MediaItem updateItem(Long itemId, Long userId, MediaItemRequest request) {
        MediaItem item = mediaItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para editar este item");
        }

        item.setTitle(request.getTitle());
        item.setType(request.getType());
        item.setStatus(request.getStatus());
        item.setGenre(request.getGenre());
        item.setRating(request.getRating());
        item.setComment(request.getComment());

        return mediaItemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        MediaItem item = mediaItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permiso para eliminar este item");
        }

        mediaItemRepository.delete(item);
    }
}