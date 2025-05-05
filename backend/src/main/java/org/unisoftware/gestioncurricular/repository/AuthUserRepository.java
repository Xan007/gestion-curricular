package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.AuthUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    @EntityGraph(attributePaths = {"userDetails", "userRole"})
    List<AuthUser> findAll();

    @EntityGraph(attributePaths = {"userDetails", "userRole"})
    Optional<AuthUser> findById(UUID id);
}
