package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.NotificationResponse;
import com.cultureSL.CultureLog.exception.ResourceNotFoundException;
import com.cultureSL.CultureLog.exception.UnauthorizedException;
import com.cultureSL.CultureLog.model.Notification;
import com.cultureSL.CultureLog.model.enums.NotificationType;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.repository.NotificationRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de notificaciones internas (In-App).
 * <p>
 * Gestiona la creación, recuperación y marcado de lectura de las notificaciones
 * generadas por eventos sociales (likes, follows, comentarios).
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Crea y persiste una nueva notificación en la base de datos.
     * <p>
     * Se ignora la notificación si el receptor y el actor son la misma persona.
     * </p>
     *
     * @param recipientId ID del usuario que recibirá la alerta.
     * @param actorId     ID del usuario que provocó el evento.
     * @param type        Tipo de evento (LIKE, FOLLOW, etc.).
     * @param referenceId ID de la entidad relacionada (ej. ID del post) para navegación.
     */
    @Override
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
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));

        if (!n.getRecipient().getId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para modificar esta notificación");
        }

        n.setRead(true);
        notificationRepository.save(n);
    }

    /**
     * Crea y persiste notificaciones en bloque de forma asíncrona.
     * <p>
     * Se ejecuta en un hilo separado del pool {@code taskExecutor} para no bloquear
     * la petición HTTP principal. Se ignoran los destinatarios que coincidan con el actor
     * o que no existan en la base de datos.
     * </p>
     *
     * @param recipientIds lista de IDs de los usuarios que recibirán la notificación.
     * @param actorId      ID del usuario que provocó el evento.
     * @param type         tipo de evento (NUEVO_POST, LIKE_POST, etc.).
     * @param referenceId  ID de la entidad relacionada (ej. ID del post) para navegación.
     */
    @Async("taskExecutor")
    @Transactional
    @Override
    public void createBulkNotificationsAsync(List<Long> recipientIds, Long actorId, NotificationType type, Long referenceId) {
        try {
            User actor = userRepository.findById(actorId).orElse(null);
            if (actor == null) return;

            List<Long> filteredIds = recipientIds.stream()
                    .filter(id -> !id.equals(actorId))
                    .toList();

            if (filteredIds.isEmpty()) return;

            Map<Long, User> recipientMap = userRepository.findAllById(filteredIds).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));

            List<Notification> notifications = filteredIds.stream()
                    .map(recipientId -> {
                        User recipient = recipientMap.get(recipientId);
                        if (recipient == null) return null;
                        Notification n = new Notification();
                        n.setRecipient(recipient);
                        n.setActor(actor);
                        n.setType(type);
                        n.setReferenceId(referenceId);
                        return n;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            notificationRepository.saveAll(notifications);
        } catch (Exception e) {
            log.error("Error al crear notificaciones en bloque para actor {}: {}", actorId, e.getMessage(), e);
        }
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