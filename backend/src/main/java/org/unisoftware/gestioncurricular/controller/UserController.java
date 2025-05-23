package org.unisoftware.gestioncurricular.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.dto.UserDTO;
import org.unisoftware.gestioncurricular.security.auth.SecurityFilter;
import org.unisoftware.gestioncurricular.util.enums.AppRole;
import org.unisoftware.gestioncurricular.security.util.SecurityUtil;
import org.unisoftware.gestioncurricular.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas con los usuarios")
public class UserController {

    private final UserService userService;
    private final SecurityFilter securityFilter;

    @Operation(
            summary = "Listar usuarios",
            description = """
    Obtiene una lista de usuarios. Se puede aplicar uno o más de los siguientes filtros:
    
    - **Rol**: Filtra los usuarios que tienen un rol específico (`DECANO`, `DIRECTOR`, `COMITE`, etc.).
    - **Nombre**: Realiza una búsqueda parcial e insensible a mayúsculas/minúsculas en los nombres o apellidos del usuario.
    - **Email**: Realiza una búsqueda parcial e insensible a mayúsculas/minúsculas por dirección de correo electrónico.
    
    Si no se especifica ningún filtro, se devuelven todos los usuarios.
    """
    )
    @GetMapping
    public List<UserDTO> getUsers(
            @Parameter(description = "Rol para filtrar los usuarios")
            @RequestParam(required = false) AppRole role,

            @Parameter(description = "Búsqueda parcial por nombre o apellido del usuario (insensible a mayúsculas)")
            @RequestParam(required = false) String name,

            @Parameter(description = "Búsqueda parcial por correo electrónico del usuario (insensible a mayúsculas)")
            @RequestParam(required = false) String email
    ) {
        if (role != null || name != null || email != null) {
            return userService.searchUsers(role, name, email);
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
    public ResponseEntity<Void> assignRole(
            @Parameter(description = "ID del usuario")
            @PathVariable UUID id,
            @Parameter(description = "Rol que se desea asignar")
            @RequestParam AppRole role) {
        String jwtToken = SecurityUtil.getJwtFromSecurityContext();

        if (jwtToken == null || jwtToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (id.equals(SecurityUtil.getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        userService.assignRole(id, role, jwtToken);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('DECANO')")
    @Operation(
            summary = "Remover roles de usuario",
            description = "Elimina todos los roles asignados a un usuario específico. **Requiere rol 'DECANO'.**"
    )
    @DeleteMapping("/{id}/remove-role")
    public ResponseEntity<Void> removeRoles(
            @Parameter(description = "ID del usuario")
            @PathVariable UUID id
    ) {
        String jwtToken = SecurityUtil.getJwtFromSecurityContext();

        if (jwtToken == null || jwtToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (id.equals(SecurityUtil.getCurrentUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        userService.removeRole(id, jwtToken);
        return ResponseEntity.noContent().build();
    }
}
