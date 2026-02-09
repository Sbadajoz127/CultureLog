package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de salida para mostrar notificaciones en la barra de navegación o panel de alertas.
 * <p>
 * Proporciona un mensaje formateado y referencias para que el frontend pueda
 * navegar al contenido relevante al hacer clic.
 * </p>
 */
@Data
@Builder
public class NotificationResponse {
    /** ID de la notificación. */
    private Long id;
    /** Nombre del usuario que generó el evento. */
    private String actorName;
    /** Mensaje descriptivo (ej. "ha comenzado a seguirte"). */
    private String message;
    /** Tipo de notificación (para elegir icono o color). */
    private NotificationType type;
    /** ID del objeto relacionado (Post ID, etc.) para la navegación. */
    private Long referenceId;
    /** Estado de lectura. */
    private boolean isRead;
    /** Fecha del evento. */
    private LocalDateTime createdAt;
}