package org.unisoftware.gestioncurricular.security.role;

import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.entity.UserRole;
import org.unisoftware.gestioncurricular.repository.UserRoleRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    /** Devuelve la lista de roles (como strings) para un usuario dado */
    public List<String> getRolesForUser(UUID userId) {
        List<UserRole> roles = userRoleRepository.findByUserId(userId);
        return roles.stream()
                .map(r -> r.getRole().name())
                .collect(Collectors.toList());
    }

    public List<UUID> getUserIdsByRole(AppRole role) {
        return userRoleRepository.findByRole(role)
                .stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
    }

    public void assignRoleToUser(UUID userId, AppRole role) {
        if (userRoleRepository.existsByUserIdAndRole(userId, role)) {
            return; // Ya tiene el rol
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }

    public void removeAllRoles(UUID userId) {
        var roles = userRoleRepository.findByUserId(userId);
        userRoleRepository.deleteAll(roles);
    }
}
