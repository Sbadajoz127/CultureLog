package com.cultureSL.CultureLog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cultureSL.CultureLog.dto.NotificationResponse;
import com.cultureSL.CultureLog.model.enums.NotificationType;

public interface NotificationService {
    
    public void createNotification(Long recipientId, Long actorId, NotificationType type, Long referenceId);

    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);

    public long getUnreadCount(Long userId);

    public void markAsRead(Long notificationId);
}
