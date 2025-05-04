package org.unisoftware.gestioncurricular.security.role;

import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;
import org.unisoftware.gestioncurricular.entity.UserRole;
import org.unisoftware.gestioncurricular.repository.UserRoleRepository;
import org.unisoftware.gestioncurricular.util.enums.AppRole;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final SupabaseProperties supabaseProperties;

    public UserRoleService(UserRoleRepository userRoleRepository, SupabaseProperties supabaseProperties) {
        this.userRoleRepository = userRoleRepository;
        this.supabaseProperties = supabaseProperties;
    }

    public AppRole getRoleForUser(UUID userId) {
        UserRole role = userRoleRepository.findByUserId(userId);
        return role != null ? role.getRole() : null;
    }


    public List<UUID> getUserIdsByRole(AppRole role) {
        List<UUID> userIds = userRoleRepository.findByRole(role)
                .stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            throw new RuntimeException("No se encontraron usuarios con el rol: " + role);
        }
        return userIds;
    }

    public void assignRoleToUser(UUID userId, AppRole newRole, String jwtToken) {
        System.out.println(jwtToken);
        if (jwtToken != null && !jwtToken.isEmpty()) {
            assignRoleWithHttp(userId, newRole, jwtToken);
        } else {
            assignRoleWithRepository(userId, newRole);
        }
    }

    public void assignRoleToUser(UUID userId, AppRole newRole) {
        assignRoleWithRepository(userId, newRole);
    }

    private void assignRoleWithHttp(UUID userId, AppRole newRole, String jwtToken) {
        try {
            UserRole existingRole = userRoleRepository.findByUserId(userId);
            if (existingRole != null) {
                if (existingRole.getRole() != newRole) {
                    updateRoleInSupabase(userId, newRole, jwtToken);
                }
            } else {
                insertRoleInSupabase(userId, newRole, jwtToken);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al asignar el rol con solicitud HTTP a Supabase para el usuario con ID: " + userId, e);
        }
    }

    private void assignRoleWithRepository(UUID userId, AppRole newRole) {
        UserRole existingRole = userRoleRepository.findByUserId(userId);
        if (existingRole != null) {
            if (existingRole.getRole() != newRole) {
                existingRole.setRole(newRole);
                userRoleRepository.save(existingRole);
            }
        } else {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRole(newRole);
            userRoleRepository.save(userRole);
        }
    }

    private void updateRoleInSupabase(UUID userId, AppRole newRole, String jwtToken) {
        try {
            String url = "https://fexiivjyzplakakkiyqm.supabase.co/rest/v1/user_roles?user_id=eq." + userId.toString();
            String body = String.format("{ \"role\": \"%s\" }", newRole.name());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("apikey", supabaseProperties.getAnonKey())
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error actualizando el rol en Supabase para el usuario con ID " + userId + ": " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el rol en Supabase para el usuario con ID: " + userId, e);
        }
    }

    private void insertRoleInSupabase(UUID userId, AppRole newRole, String jwtToken) {
        try {
            String url = "https://fexiivjyzplakakkiyqm.supabase.co/rest/v1/user_roles";
            String body = String.format("{ \"user_id\": \"%s\", \"role\": \"%s\" }", userId.toString(), newRole.name());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("apikey", supabaseProperties.getAnonKey())
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .method("POST", HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Error insertando el rol en Supabase para el usuario con ID " + userId + ": " + response.body());
            }

            System.out.println("Rol insertado correctamente en Supabase para el usuario con ID: " + userId);
        } catch (Exception e) {
            System.out.println("insert" + e.getMessage());
            throw new RuntimeException("Error al insertar el rol en Supabase para el usuario con ID: " + userId, e);
        }
    }

    public void removeRole(UUID userId) {
        UserRole role = userRoleRepository.findByUserId(userId);
        if (role == null) {
            throw new RuntimeException("No se encontró un rol para eliminar para el usuario con ID: " + userId);
        }
        userRoleRepository.delete(role);
    }
}
