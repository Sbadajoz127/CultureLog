package com.cultureSL.CultureLog.service.impl;

import com.cultureSL.CultureLog.dto.NotificationResponse;
import com.cultureSL.CultureLog.model.Notification;
import com.cultureSL.CultureLog.model.enums.NotificationType;
import com.cultureSL.CultureLog.model.User;
import com.cultureSL.CultureLog.repository.NotificationRepository;
import com.cultureSL.CultureLog.repository.UserRepository;
import com.cultureSL.CultureLog.service.NotificationService; // Crea esta interfaz primero
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Async
    @Transactional
    public void createNotification(Long recipientId, Long actorId, NotificationType type, Long referenceId) {
        if (recipientId.equals(actorId)) return;

        User recipient = userRepository.findById(recipientId).orElseThrow();
        User actor = userRepository.findById(actorId).orElseThrow();

        Notification notif = new Notification();
        notif.setRecipient(recipient);
        notif.setActor(actor);
        notif.setType(type);
        notif.setReferenceId(referenceId);

        notificationRepository.save(notif);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

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