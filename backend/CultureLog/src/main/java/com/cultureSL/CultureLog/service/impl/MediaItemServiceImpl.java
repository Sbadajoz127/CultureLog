package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.MediaItemRequest;
import com.cultureSL.CultureLog.exception.DuplicateItemException;
import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
import com.cultureSL.CultureLog.exception.UnauthorizedException;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.repository.MediaItemRepository;
import com.cultureSL.CultureLog.repository.PostRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.ImageStorageService;
import com.cultureSL.CultureLog.service.MediaItemService;
import com.cultureSL.CultureLog.service.MediaLibraryDuplicateFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación del servicio de gestión de la biblioteca multimedia.
 * <p>
 * Proporciona las operaciones CRUD sobre los ítems multimedia del usuario,
 * incluyendo filtrado por tipo y estado, y validación de pertenencia.
 * </p>
 *
 * @see MediaItemService
 */
@Service
@RequiredArgsConstructor
public class MediaItemServiceImpl implements MediaItemService {

    private final MediaItemRepository mediaItemRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ImageStorageService imageStorageService;
    private final MediaLibraryDuplicateFinder duplicateFinder;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MediaItem> getUserItems(Long userId) {
        return mediaItemRepository.findByUserIdWithTags(userId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MediaItem> filterItems(Long userId, MediaType type, MediaStatus status) {
        if (type != null && status != null) {
            return mediaItemRepository.findByUserIdAndTypeAndStatusWithTags(userId, type, status);
        } else if (type != null) {
            return mediaItemRepository.findByUserIdAndTypeWithTags(userId, type);
        } else if (status != null) {
            return mediaItemRepository.findByUserIdAndStatusWithTags(userId, status);
        }
        return getUserItems(userId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MediaItem addItem(Long userId, MediaItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (duplicateFinder
                .findDuplicate(
                        userId,
                        request.getType(),
                        request.getTitle(),
                        request.getExternalId(),
                        request.getExternalSource())
                .isPresent()) {
            throw new DuplicateItemException("Este ítem ya está en tu biblioteca");
        }

        MediaItem item = new MediaItem();
        item.setTitle(request.getTitle());
        item.setItemImageUrl(request.getItemImageUrl());
        item.setType(request.getType());
        item.setStatus(request.getStatus());
        item.setGenre(request.getGenre());
        item.setCreator(request.getCreator());
        item.setRating(request.getRating());
        item.setReleaseDate(request.getReleaseDate());
        item.setComment(request.getComment());
        item.setDescription(request.getDescription());
        item.setExternalId(request.getExternalId());
        item.setExternalSource(request.getExternalSource());
        item.setDateAdded(LocalDate.now());
        item.setUser(user);

        return mediaItemRepository.save(item);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MediaItem updateItem(Long itemId, Long userId, MediaItemRequest request) {
        MediaItem item = mediaItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        if (!item.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para editar este item");
        }

        item.setTitle(request.getTitle());
        item.setItemImageUrl(request.getItemImageUrl());
        item.setType(request.getType());
        item.setStatus(request.getStatus());
        item.setGenre(request.getGenre());
        item.setCreator(request.getCreator());
        item.setRating(request.getRating());
        item.setReleaseDate(request.getReleaseDate());
        item.setComment(request.getComment());
        item.setDescription(request.getDescription());
        item.setExternalId(request.getExternalId());
        item.setExternalSource(request.getExternalSource());

        return mediaItemRepository.save(item);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        MediaItem item = mediaItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        if (!item.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para eliminar este item");
        }

        postRepository.unlinkMediaItem(itemId);

        if (item.getItemImageUrl() != null) {
            imageStorageService.deleteImage(item.getItemImageUrl());
        }

        mediaItemRepository.delete(item);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void removeMediaItemImage(Long itemId, Long userId) {
        MediaItem item = mediaItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado"));

        if (!item.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para modificar este item");
        }

        if (item.getItemImageUrl() != null) {
            imageStorageService.deleteImage(item.getItemImageUrl());
        }

        item.setItemImageUrl(null);
        mediaItemRepository.save(item);
    }
}
