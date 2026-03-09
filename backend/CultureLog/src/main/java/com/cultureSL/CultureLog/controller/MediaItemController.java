package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.MediaItemRequest;
import com.cultureSL.CultureLog.dto.MediaItemResponse;
import com.cultureSL.CultureLog.mapper.MediaItemMapper;
import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import com.cultureSL.CultureLog.service.MediaItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de la biblioteca multimedia del usuario.
 * <p>
 * Expone endpoints CRUD para los ítems multimedia, incluyendo filtrado
 * por tipo y estado. Requiere autenticación JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class MediaItemController {

    private final MediaItemService mediaItemService;
    private final MediaItemMapper mediaItemMapper;

    /**
     * Obtiene los ítems multimedia del usuario autenticado, con filtros opcionales.
     * <p>Endpoint: {@code GET /api/items?type=...&status=...}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param type           filtro por tipo de medio (opcional)
     * @param status         filtro por estado de consumo (opcional)
     * @return HTTP 200 con la lista de ítems en formato {@link MediaItemResponse}
     */
    @GetMapping
    public ResponseEntity<List<MediaItemResponse>> getUserItems(
            Authentication authentication,
            @RequestParam(required = false) MediaType type,
            @RequestParam(required = false) MediaStatus status) {

        Long userId = (Long) authentication.getPrincipal();
        List<MediaItem> items;

        if (type == null && status == null) {
            items = mediaItemService.getUserItems(userId);
        } else {
            items = mediaItemService.filterItems(userId, type, status);
        }

        return ResponseEntity.ok(mediaItemMapper.toDtoList(items));
    }

    /**
     * Añade un nuevo ítem multimedia a la biblioteca del usuario.
     * <p>Endpoint: {@code POST /api/items}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param request        datos del ítem a crear
     * @return HTTP 200 con el ítem creado en formato {@link MediaItemResponse}
     */
    @PostMapping
    public ResponseEntity<MediaItemResponse> addItem(
            Authentication authentication,
            @Valid @RequestBody MediaItemRequest request) {

        Long userId = (Long) authentication.getPrincipal();
        MediaItem item = mediaItemService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaItemMapper.toDto(item));
    }

    /**
     * Actualiza los datos de un ítem multimedia existente.
     * <p>Endpoint: {@code PUT /api/items/{itemId}}</p>
     *
     * @param itemId         ID del ítem a actualizar
     * @param authentication contexto de autenticación con el ID del usuario
     * @param request        nuevos datos del ítem
     * @return HTTP 200 con el ítem actualizado en formato {@link MediaItemResponse}
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<MediaItemResponse> updateItem(
            @PathVariable Long itemId,
            Authentication authentication,
            @Valid @RequestBody MediaItemRequest request) {

        Long userId = (Long) authentication.getPrincipal();
        MediaItem item = mediaItemService.updateItem(itemId, userId, request);
        return ResponseEntity.ok(mediaItemMapper.toDto(item));
    }

    /**
     * Elimina la imagen asociada a un ítem multimedia.
     * <p>Endpoint: {@code DELETE /api/items/{itemId}/image}</p>
     *
     * @param itemId         ID del ítem
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 sin contenido
     */
    @DeleteMapping("/{itemId}/image")
    public ResponseEntity<Void> removeImage(
            @PathVariable Long itemId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        mediaItemService.removeMediaItemImage(itemId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Elimina un ítem multimedia de la biblioteca del usuario.
     * <p>Endpoint: {@code DELETE /api/items/{itemId}}</p>
     *
     * @param itemId         ID del ítem a eliminar
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 sin contenido
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long itemId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        mediaItemService.deleteItem(itemId, userId);
        return ResponseEntity.ok().build();
    }
}
