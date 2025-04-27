package org.unisoftware.gestioncurricular.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.dto.UserDTO;
import org.unisoftware.gestioncurricular.entity.UserDetails;
import org.unisoftware.gestioncurricular.repository.UserDetailsRepository;
import org.unisoftware.gestioncurricular.repository.UserRoleRepository;
import org.unisoftware.gestioncurricular.security.role.AppRole;
import org.unisoftware.gestioncurricular.security.role.UserRoleService;
import org.unisoftware.gestioncurricular.security.user.AuthUserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthUserRepository authUserRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final UserRoleService userRoleService;

    public UserDTO getUser(UUID userId) {
        var authUser = authUserRepository.findById(userId);
        var userDetails = userDetailsRepository.findByUserId(userId).orElse(null);
        var roles = userRoleService.getRolesForUser(userId);

        return mapToDTO(authUser.getId(), authUser.getEmail(), authUser.getCreatedAt(), userDetails, roles);
    }

    public List<UserDTO> getAllUsers() {
        var authUsers = authUserRepository.findAll();
        return authUsers.stream()
                .map(authUser -> {
                    var details = userDetailsRepository.findByUserId(authUser.getId()).orElse(null);
                    var roles = userRoleService.getRolesForUser(authUser.getId());
                    return mapToDTO(authUser.getId(), authUser.getEmail(), authUser.getCreatedAt(), details, roles);
                })
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(AppRole role) {
        var userIds = userRoleService.getUserIdsByRole(role);
        return userIds.stream()
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    public void assignRole(UUID userId, AppRole role) {
        userRoleService.assignRoleToUser(userId, role);
    }

    public void removeAllRoles(UUID userId) {
        userRoleService.removeAllRoles(userId);
    }

    private UserDTO mapToDTO(UUID id, String email, java.time.Instant createdAt, UserDetails details, List<String> roles) {
        return new UserDTO(
                id,
                email,
                createdAt,
                details != null ? details.getPrimerNombre() : null,
                details != null ? details.getSegundoNombre() : null,
                details != null ? details.getPrimerApellido() : null,
                details != null ? details.getSegundoApellido() : null,
                roles
        );
    }

    public List<AppRole> getUserRoles(UUID userId) {
        return userRoleService.getRolesForUser(userId)
                .stream()
                .map(AppRole::valueOf)
                .collect(Collectors.toList());
    }
}
