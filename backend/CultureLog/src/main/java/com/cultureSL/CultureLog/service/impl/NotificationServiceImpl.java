package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.NotificationResponse;
import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
import com.cultureSL.CultureLog.model.Notification;
import com.cultureSL.CultureLog.model.enums.NotificationType;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.repository.NotificationRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de notificaciones internas (In-App).
 * <p>
 * Gestiona la creación, recuperación y marcado de lectura de las notificaciones
 * generadas por eventos sociales (likes, follows, comentarios).
 * </p>
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Crea y persiste una nueva notificación en la base de datos.
     * <p>
     * Este método es asíncrono para no bloquear la transacción principal del evento que lo dispara.
     * Se ignora la notificación si el receptor y el actor son la misma persona.
     * </p>
     *
     * @param recipientId ID del usuario que recibirá la alerta.
     * @param actorId     ID del usuario que provocó el evento.
     * @param type        Tipo de evento (LIKE, FOLLOW, etc.).
     * @param referenceId ID de la entidad relacionada (ej. ID del post) para navegación.
     */
    @Override
    @Async
    @Transactional
    public void createNotification(Long recipientId, Long actorId, NotificationType type, Long referenceId) {
        if (recipientId.equals(actorId)) return;

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario destinatario no encontrado"));
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario actor no encontrado"));

        Notification notif = new Notification();
        notif.setRecipient(recipient);
        notif.setActor(actor);
        notif.setType(type);
        notif.setReferenceId(referenceId);

        notificationRepository.save(notif);
    }

    /**
     * Recupera las notificaciones de un usuario de forma paginada y ordenadas por fecha reciente.
     *
     * @param userId   ID del usuario.
     * @param pageable Configuración de paginación.
     * @return Página de objetos {@link NotificationResponse} (DTOs) listos para la vista.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    /**
     * Cuenta el número de notificaciones que el usuario aún no ha marcado como leídas.
     * Útil para mostrar badges o contadores en la interfaz.
     *
     * @param userId ID del usuario.
     * @return Número de notificaciones sin leer.
     */
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    /**
     * Marca una notificación específica como leída.
     *
     * @param notificationId ID de la notificación.
     */
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    /**
     * Método auxiliar para convertir la entidad {@link Notification} en un DTO {@link NotificationResponse}.
     * Construye el mensaje de texto legible basado en el tipo de notificación.
     *
     * @param n Entidad notificación.
     * @return DTO formateado.
     */
    private NotificationResponse mapToDto(Notification n) {
        String text = switch (n.getType()) {
            case NUEVO_SEGUIDOR -> "ha comenzado a seguirte.";
            case LIKE_POST -> "le ha gustado tu publicación.";
            case COMENTARIO_POST -> "ha comentado en tu publicación.";
            case NUEVO_POST -> "ha publicado un nuevo post.";
        };

        return NotificationResponse.builder()
                .id(n.getId())
                .actorName(n.getActor().getUsername())
                .message(text)
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}