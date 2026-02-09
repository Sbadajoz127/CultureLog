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

/**
 * Controlador REST para la gestión de la biblioteca multimedia personal.
 * <p>
 * Proporciona operaciones CRUD completas para items multimedia (Libros, Películas, etc.)
 * permitiendo filtrado y actualización.
 * </p>
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class MediaItemController {

    private final MediaItemService mediaItemService;

    /**
     * Obtiene la lista de items de la biblioteca de un usuario, con filtros opcionales.
     * <p>Endpoint: {@code GET /api/items}</p>
     *
     * @param userId ID del usuario propietario de la biblioteca.
     * @param type   (Opcional) Filtrar por tipo de medio (ej. LIBRO).
     * @param status (Opcional) Filtrar por estado (ej. POR_VER).
     * @return Lista de {@link MediaItem} filtrada.
     */
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

    /**
     * Añade un nuevo ítem a la biblioteca del usuario.
     * <p>Endpoint: {@code POST /api/items}</p>
     *
     * @param userId  ID del usuario.
     * @param request Datos del nuevo ítem.
     * @return El ítem creado.
     */
    @PostMapping
    public ResponseEntity<MediaItem> addItem(
            @RequestParam Long userId, 
            @RequestBody MediaItemRequest request) {
        return ResponseEntity.ok(mediaItemService.addItem(userId, request));
    }

    /**
     * Actualiza los datos de un ítem existente.
     * <p>Endpoint: {@code PUT /api/items/{itemId}}</p>
     *
     * @param itemId  ID del ítem a modificar.
     * @param userId  ID del usuario (para verificación de permisos).
     * @param request Nuevos datos del ítem.
     * @return El ítem actualizado.
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<MediaItem> updateItem(
            @PathVariable Long itemId,
            @RequestParam Long userId,
            @RequestBody MediaItemRequest request) {
        return ResponseEntity.ok(mediaItemService.updateItem(itemId, userId, request));
    }

    /**
     * Elimina un ítem de la biblioteca.
     * <p>Endpoint: {@code DELETE /api/items/{itemId}}</p>
     *
     * @param itemId ID del ítem a eliminar.
     * @param userId ID del usuario (para verificación de permisos).
     * @return {@code 200 OK} si se elimina correctamente.
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long itemId,
            @RequestParam Long userId) {
        mediaItemService.deleteItem(itemId, userId);
        return ResponseEntity.ok().build();
    }
}