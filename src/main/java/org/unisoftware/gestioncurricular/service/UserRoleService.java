package org.unisoftware.gestioncurricular.service;

import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.entity.UserRole;
import org.unisoftware.gestioncurricular.model.AppRole;
import org.unisoftware.gestioncurricular.repository.UserRoleRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserRoleService {

    private final UserRoleRepository repo;

    public UserRoleService(UserRoleRepository repo) {
        this.repo = repo;
    }

    /** Devuelve la lista de roles (como strings) para un usuario dado */
    public List<String> getRolesForUser(UUID userId) {
        List<UserRole> roles = repo.findByUserId(userId);
        return roles.stream()
                .map(r -> r.getRole().name())
                .collect(Collectors.toList());
    }
}
