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

    public AppRole getRoleForUser(UUID userId) {
        UserRole role = userRoleRepository.findByUserId(userId);
        if (role == null) {
            return null;
        }
        return role.getRole();
    }

    public List<UUID> getUserIdsByRole(AppRole role) {
        return userRoleRepository.findByRole(role)
                .stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
    }

    public void assignRoleToUser(UUID userId, AppRole newRole) {
        UserRole existingRole = userRoleRepository.findByUserId(userId);

        // Si ya tiene un rol, actualízalo si es distinto
        if (existingRole != null) {
            if (existingRole.getRole() != newRole) {
                existingRole.setRole(newRole);
                userRoleRepository.save(existingRole); // UPDATE
            }
            return;
        }

        // Si no tiene ningún rol, lo creamos
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRole(newRole);
        userRoleRepository.save(userRole);
    }

    public void removeRole(UUID userId) {
        UserRole role = userRoleRepository.findByUserId(userId);
        userRoleRepository.delete(role);
    }
}
