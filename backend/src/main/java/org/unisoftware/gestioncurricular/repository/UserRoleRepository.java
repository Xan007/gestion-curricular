package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.UserRole;
import org.unisoftware.gestioncurricular.security.role.AppRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserId(UUID userId);

    Optional<UserRole> findByUserIdAndRole(UUID userId, AppRole role);

    List<UserRole> findByRole(AppRole role);

    boolean existsByUserIdAndRole(UUID userId, AppRole role);
}
