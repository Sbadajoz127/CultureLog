package com.cultureSL.CultureLog.service.search.impl;

import com.cultureSL.CultureLog.dto.MediaItemRequest;
import com.cultureSL.CultureLog.dto.MediaItemResponse;
import com.cultureSL.CultureLog.dto.search.MediaSearchResult;
import com.cultureSL.CultureLog.exception.DuplicateItemException;
import com.cultureSL.CultureLog.mapper.MediaItemMapper;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.repository.MediaItemRepository;
import com.cultureSL.CultureLog.service.ImageStorageService;
import com.cultureSL.CultureLog.service.MediaItemService;
import com.cultureSL.CultureLog.service.search.ExternalMediaProvider;
import com.cultureSL.CultureLog.service.search.MediaSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Implementación del servicio de búsqueda multimedia en APIs externas (patrón Facade).
 * <p>
 * Inyecta todos los {@link ExternalMediaProvider} registrados en el contexto de Spring
 * y los orquesta según el tipo de medio solicitado. Cuando no se filtra por tipo,
 * las búsquedas se ejecutan en paralelo mediante {@link CompletableFuture} para
 * minimizar la latencia total.
 * </p>
 *
 * @see MediaSearchService
 * @see ExternalMediaProvider
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaSearchServiceImpl implements MediaSearchService {

    private final List<ExternalMediaProvider> providers;
    private final MediaItemService mediaItemService;
    private final MediaItemRepository mediaItemRepository;
    private final ImageStorageService imageStorageService;
    private final MediaItemMapper mediaItemMapper;

    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    /** {@inheritDoc} */
    @Override
    public List<MediaSearchResult> search(String query, MediaType type, int page) {
        List<ExternalMediaProvider> targetProviders = resolveProviders(type);

        if (targetProviders.isEmpty()) {
            return Collections.emptyList();
        }

        if (targetProviders.size() == 1) {
            return targetProviders.getFirst().search(query, page);
        }

        List<CompletableFuture<List<MediaSearchResult>>> futures = targetProviders.stream()
                .map(provider -> CompletableFuture.supplyAsync(
                        () -> provider.search(query, page), taskExecutor)
                        .exceptionally(ex -> {
                            log.error("Error en provider {}: {}", provider.getClass().getSimpleName(), ex.getMessage());
                            return Collections.emptyList();
                        }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<MediaSearchResult> results = new ArrayList<>();
        for (CompletableFuture<List<MediaSearchResult>> future : futures) {
            results.addAll(future.join());
        }
        return results;
    }

    /** {@inheritDoc} */
    @Override
    public MediaItemResponse addToLibrary(Long userId, MediaSearchResult searchResult) {
        if (searchResult.getExternalId() != null && searchResult.getSource() != null
                && mediaItemRepository.existsByUserIdAndExternalIdAndExternalSource(
                        userId, searchResult.getExternalId(), searchResult.getSource())) {
            throw new DuplicateItemException("Este ítem ya está en tu biblioteca");
        }

        String cloudinaryUrl = null;
        if (searchResult.getImageUrl() != null && !searchResult.getImageUrl().isBlank()) {
            try {
                cloudinaryUrl = imageStorageService.uploadImageFromUrl(searchResult.getImageUrl());
            } catch (Exception e) {
                log.warn("No se pudo subir la imagen a Cloudinary, se continuará sin imagen: {}", e.getMessage());
            }
        }

        MediaItemRequest request = new MediaItemRequest();
        request.setTitle(searchResult.getTitle());
        request.setType(searchResult.getType());
        request.setStatus(MediaStatus.POR_VER);
        request.setGenre(searchResult.getGenre());
        request.setCreator(searchResult.getCreator());
        request.setComment(null);
        request.setReleaseDate(searchResult.getReleaseDate());
        request.setDescription(searchResult.getDescription());
        request.setItemImageUrl(cloudinaryUrl);
        request.setExternalId(searchResult.getExternalId());
        request.setExternalSource(searchResult.getSource());

        try {
            MediaItem savedItem = mediaItemService.addItem(userId, request);
            return mediaItemMapper.toDto(savedItem);
        } catch (Exception e) {
            if (cloudinaryUrl != null) {
                try {
                    imageStorageService.deleteImage(cloudinaryUrl);
                } catch (Exception cleanupEx) {
                    log.warn("No se pudo eliminar la imagen huérfana de Cloudinary: {}", cleanupEx.getMessage());
                }
            }
            throw e;
        }
    }

    /**
     * Selecciona los proveedores adecuados para el tipo de medio solicitado.
     *
     * @param type tipo de medio a filtrar; {@code null} devuelve todos los proveedores
     * @return lista de proveedores que soportan el tipo indicado
     */
    private List<ExternalMediaProvider> resolveProviders(MediaType type) {
        if (type == null) {
            return providers;
        }
        return providers.stream()
                .filter(p -> p.getSupportedTypes().contains(type))
                .toList();
    }
}
