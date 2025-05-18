package org.unisoftware.gestioncurricular.repository;

import org.springframework.stereotype.Repository;
import org.unisoftware.gestioncurricular.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}