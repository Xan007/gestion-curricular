package org.unisoftware.gestioncurricular.service;

import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.entity.Notification;
import org.unisoftware.gestioncurricular.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getNotificationsByUserId(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void sendNotification(UUID userId, String title, String description) {
        Notification notification = new Notification(userId, title, description);
        notificationRepository.save(notification);
    }

    public void markAllAsSeenByUserId(UUID userId) {
        notificationRepository.markAllAsSeenByUserId(userId);
    }
}
