package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.NotificationResponse;
import com.cultureSL.CultureLog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el sistema de notificaciones In-App.
 * <p>
 * Permite al cliente consultar alertas y gestionar su estado de lectura.
 * </p>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Obtiene el historial de notificaciones de un usuario (paginado).
     * <p>Endpoint: {@code GET /api/notifications}</p>
     *
     * @param userId   ID del usuario.
     * @param pageable Parámetros de paginación (page, size).
     * @return Página de notificaciones.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @RequestParam Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    /**
     * Obtiene el número de notificaciones pendientes de leer.
     * <p>Endpoint: {@code GET /api/notifications/unread-count}</p>
     *
     * @param userId ID del usuario.
     * @return Cantidad de notificaciones no leídas (Long).
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }
    
    /**
     * Marca una notificación específica como leída.
     * <p>Endpoint: {@code POST /api/notifications/{id}/read}</p>
     *
     * @param id ID de la notificación.
     * @return {@code 200 OK}.
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}