package org.unisoftware.gestioncurricular.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.unisoftware.gestioncurricular.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.seen = true WHERE n.userId = :userId AND n.seen = false")
    void markAllAsSeenByUserId(@Param("userId") UUID userId);
}