package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.NotificationResponse;
import com.cultureSL.CultureLog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de notificaciones del usuario.
 * <p>
 * Permite consultar el historial de notificaciones paginado, obtener el
 * contador de no leídas y marcar notificaciones como leídas.
 * Requiere autenticación JWT.
 * </p>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Obtiene las notificaciones del usuario autenticado de forma paginada.
     * <p>Endpoint: {@code GET /api/notifications?page=0&size=20}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @param pageable       configuración de paginación
     * @return HTTP 200 con la página de {@link NotificationResponse}
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            Authentication authentication,
            Pageable pageable) {

        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }

    /**
     * Obtiene el número de notificaciones no leídas del usuario.
     * <p>Endpoint: {@code GET /api/notifications/unread-count}</p>
     *
     * @param authentication contexto de autenticación con el ID del usuario
     * @return HTTP 200 con el contador de notificaciones sin leer
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    /**
     * Marca una notificación como leída.
     * <p>Endpoint: {@code POST /api/notifications/{id}/read}</p>
     *
     * @param id ID de la notificación a marcar
     * @return HTTP 200 sin contenido
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
