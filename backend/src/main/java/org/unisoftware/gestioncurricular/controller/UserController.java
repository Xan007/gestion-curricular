package org.unisoftware.gestioncurricular.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.dto.UserDTO;
import org.unisoftware.gestioncurricular.security.role.AppRole;
import org.unisoftware.gestioncurricular.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas con los usuarios")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Listar usuarios", description = "Obtiene una lista de usuarios. Puede filtrarse por rol si se pasa como parámetro.")
    @GetMapping
    public List<UserDTO> getUsers(
            @Parameter(description = "Rol para filtrar los usuarios")
            @RequestParam(required = false) AppRole role) {
        if (role != null) {
            return userService.getUsersByRole(role);
        }
        return userService.getAllUsers();
    }

    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los datos de un usuario específico por su ID.")
    @GetMapping("/{id}")
    public UserDTO getUser(
            @Parameter(description = "ID del usuario")
            @PathVariable UUID id) {
        return userService.getUser(id);
    }

    @Operation(summary = "Obtener roles de un usuario", description = "Obtiene la lista de roles asignados a un usuario por su ID.")
    @GetMapping("/{id}/role")
    public AppRole getUserRoles(
            @Parameter(description = "ID del usuario")
            @PathVariable UUID id) {
        return userService.getUserRole(id);
    }

    @Operation(summary = "Obtener roles del usuario autenticado", description = "Obtiene los roles asignados al usuario actualmente autenticado.")
    @GetMapping("/me/role")
    public AppRole getMyRoles(@AuthenticationPrincipal UUID userId) {
        return userService.getUserRole(userId);
    }

    @Operation(summary = "Obtener ID del usuario autenticado", description = "Devuelve el ID (UUID) del usuario actualmente autenticado.")
    @GetMapping("/me/id")
    public UUID getMyId(@AuthenticationPrincipal UUID userId) {
        return userId;
    }

    @Operation(summary = "Obtener detalles del usuario autenticado", description = "Obtiene los detalles del usuario actualmente autenticado.")
    @GetMapping("/me/details")
    public UserDTO getMyDetails(@AuthenticationPrincipal UUID userId) {
        return userService.getUser(userId);
    }

    @PreAuthorize("hasRole('DECANO')")
    @Operation(summary = "Asignar rol a usuario", description = "Asigna un nuevo rol a un usuario específico. **Requiere rol 'DECANO'.**")
    @PostMapping("/{id}/assign-role")
    public void assignRole(
            @Parameter(description = "ID del usuario")
            @PathVariable UUID id,
            @Parameter(description = "Rol que se desea asignar")
            @RequestParam AppRole role) {
        userService.assignRole(id, role);
    }

    @PreAuthorize("hasRole('DECANO')")
    @Operation(
            summary = "Remover roles de usuario",
            description = "Elimina todos los roles asignados a un usuario específico. **Requiere rol 'DECANO'.**"
    )
    @DeleteMapping("/{id}/remove-role")
    public void removeRoles(
            @Parameter(description = "ID del usuario")
            @PathVariable UUID id
    ) {
        userService.removeRole(id);
    }
}
