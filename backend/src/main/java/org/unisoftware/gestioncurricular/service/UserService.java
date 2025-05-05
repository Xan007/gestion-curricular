package org.unisoftware.gestioncurricular.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.dto.UserDTO;
import org.unisoftware.gestioncurricular.entity.AuthUser;
import org.unisoftware.gestioncurricular.entity.UserDetails;
import org.unisoftware.gestioncurricular.entity.UserRole;
import org.unisoftware.gestioncurricular.repository.AuthUserRepository;
import org.unisoftware.gestioncurricular.repository.UserDetailsRepository;
import org.unisoftware.gestioncurricular.security.role.UserRoleService;
import org.unisoftware.gestioncurricular.util.enums.AppRole;

import java.time.Instant;
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
        return authUserRepository.findById(userId)
                .map(this::mapToDTO)
                .orElse(null);
    }

    public List<UserDTO> getAllUsers() {
        return authUserRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(AppRole role) {
        var userIds = userRoleService.getUserIdsByRole(role);
        return userIds.stream()
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    public void assignRole(UUID userId, AppRole role, String jwtToken) {
        userRoleService.assignRoleToUser(userId, role, jwtToken);
    }

    public void removeRole(UUID userId, String jwtToken) {
        userRoleService.removeRole(userId, jwtToken);
    }

    public AppRole getUserRole(UUID userId) {
        return userRoleService.getRoleForUser(userId);
    }

    // Nuevo mapToDTO usando AuthUser directamente
    private UserDTO mapToDTO(AuthUser authUser) {
        UserDetails details = authUser.getUserDetails();
        UserRole role = authUser.getUserRole();

        return new UserDTO(
                authUser.getId(),
                authUser.getEmail(),
                authUser.getCreatedAt(),
                details != null ? details.getPrimerNombre() : null,
                details != null ? details.getSegundoNombre() : null,
                details != null ? details.getPrimerApellido() : null,
                details != null ? details.getSegundoApellido() : null,
                role != null ? role.getRole() : null
        );
    }

    // Conservamos esta función privada en caso de que se requiera en alguna parte del sistema
    private UserDTO mapToDTO(UUID id, String email, Instant createdAt, UserDetails details, AppRole role) {
        return new UserDTO(
                id,
                email,
                createdAt,
                details != null ? details.getPrimerNombre() : null,
                details != null ? details.getSegundoNombre() : null,
                details != null ? details.getPrimerApellido() : null,
                details != null ? details.getSegundoApellido() : null,
                role
        );
    }
}
