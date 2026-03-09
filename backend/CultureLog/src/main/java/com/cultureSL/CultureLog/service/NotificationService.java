package com.cultureSL.CultureLog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cultureSL.CultureLog.dto.NotificationResponse;
import com.cultureSL.CultureLog.model.enums.NotificationType;

/**
 * Servicio que gestiona el sistema de notificaciones internas de la aplicación.
 * <p>
 * Define el contrato para crear, consultar y marcar como leídas las notificaciones
 * generadas por eventos sociales (likes, comentarios, nuevos seguidores, etc.).
 * </p>
 */
public interface NotificationService {

    /**
     * Crea una nueva notificación para un usuario.
     *
     * @param recipientId ID del usuario receptor de la notificación
     * @param actorId     ID del usuario que provocó el evento
     * @param type        tipo de evento ({@link NotificationType})
     * @param referenceId ID del objeto relacionado (ej: ID del post para LIKE_POST)
     */
    void createNotification(Long recipientId, Long actorId, NotificationType type, Long referenceId);

    /**
     * Obtiene las notificaciones de un usuario de forma paginada.
     *
     * @param userId   ID del usuario receptor
     * @param pageable configuración de paginación
     * @return página de notificaciones mapeadas a {@link NotificationResponse}
     */
    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

    /**
     * Obtiene el número de notificaciones no leídas de un usuario.
     *
     * @param userId ID del usuario
     * @return cantidad de notificaciones sin leer
     */
    long getUnreadCount(Long userId);

    /**
     * Marca una notificación como leída.
     *
     * @param notificationId ID de la notificación
     * @throws com.cultureSL.CultureLog.exception.ResourceNotFoundException si la notificación no existe
     */
    void markAsRead(Long notificationId);
}
