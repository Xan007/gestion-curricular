package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDetailsRepository extends JpaRepository<UserDetails, UUID> {

    @EntityGraph(attributePaths = {"authUser"})
    Optional<UserDetails> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"authUser"})
    List<UserDetails> findAll();
}
