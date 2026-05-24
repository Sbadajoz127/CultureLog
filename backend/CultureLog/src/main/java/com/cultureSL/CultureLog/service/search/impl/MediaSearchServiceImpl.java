package com.cultureSL.CultureLog.service.search.impl;

import com.cultureSL.CultureLog.dto.AddToLibraryResult;
import com.cultureSL.CultureLog.dto.MediaItemRequest;
import com.cultureSL.CultureLog.dto.search.LibraryMatchProjection;
import com.cultureSL.CultureLog.dto.search.MediaSearchResult;
import com.cultureSL.CultureLog.exception.DuplicateItemException;
import com.cultureSL.CultureLog.mapper.MediaItemMapper;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.repository.MediaItemRepository;
import com.cultureSL.CultureLog.service.ImageStorageService;
import com.cultureSL.CultureLog.service.MediaItemService;
import com.cultureSL.CultureLog.service.MediaLibraryDuplicateFinder;
import com.cultureSL.CultureLog.service.search.ExternalMediaProvider;
import com.cultureSL.CultureLog.service.search.MediaSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private final MediaLibraryDuplicateFinder duplicateFinder;
    private final ImageStorageService imageStorageService;
    private final MediaItemMapper mediaItemMapper;
    private final MediaItemRepository mediaItemRepository;

    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    /** {@inheritDoc} */
    @Override
    @Cacheable(value = "searchResults", key = "#query.toLowerCase() + '-' + #type + '-' + #page")
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
    public AddToLibraryResult addToLibrary(Long userId, MediaSearchResult searchResult) {
        var existing =
                duplicateFinder.findDuplicate(
                        userId,
                        searchResult.getType(),
                        searchResult.getTitle(),
                        searchResult.getExternalId(),
                        searchResult.getSource());
        if (existing.isPresent()) {
            return new AddToLibraryResult(false, mediaItemMapper.toDto(existing.get()));
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
        request.setAlbum(searchResult.getAlbum());

        try {
            MediaItem savedItem = mediaItemService.addItem(userId, request);
            return new AddToLibraryResult(true, mediaItemMapper.toDto(savedItem));
        } catch (DuplicateItemException race) {
            log.debug("Race condition: el ítem se insertó entre la comprobación y el addItem – se devuelve el existente");
            if (cloudinaryUrl != null) {
                try { imageStorageService.deleteImage(cloudinaryUrl); } catch (Exception ignored) { }
            }
            var fallback = duplicateFinder.findDuplicate(
                    userId, searchResult.getType(), searchResult.getTitle(),
                    searchResult.getExternalId(), searchResult.getSource());
            return new AddToLibraryResult(false, mediaItemMapper.toDto(fallback.orElseThrow()));
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

    /** {@inheritDoc} */
    @Override
    public List<MediaSearchResult> enrichWithLibraryStatus(Long userId, List<MediaSearchResult> results) {
        if (results == null || results.isEmpty()) {
            return results;
        }

        List<LibraryMatchProjection> libraryKeys = mediaItemRepository.findExternalKeysByUserId(userId);

        Map<String, MediaStatus> libraryMap = new HashMap<>();
        for (LibraryMatchProjection p : libraryKeys) {
            String key = p.getExternalSource() + "|" + p.getExternalId() + "|" + p.getType();
            libraryMap.put(key, p.getStatus());
        }

        return results.stream()
                .map(r -> {
                    if (r.getSource() == null || r.getExternalId() == null) {
                        return r;
                    }
                    String key = r.getSource() + "|" + r.getExternalId() + "|" + r.getType();
                    MediaStatus status = libraryMap.get(key);
                    if (status != null) {
                        return r.toBuilder().libraryStatus(status.name()).build();
                    }
                    return r;
                })
                .toList();
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
