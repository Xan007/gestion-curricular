package org.unisoftware.gestioncurricular.security.role;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;
import org.unisoftware.gestioncurricular.entity.UserRole;
import org.unisoftware.gestioncurricular.exception.SupabaseException;
import org.unisoftware.gestioncurricular.repository.UserRoleRepository;
import org.unisoftware.gestioncurricular.util.enums.AppRole;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserRoleService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

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
        UserRole existingRole = userRoleRepository.findByUserId(userId);
        if (existingRole != null) {
            if (existingRole.getRole() != newRole) {
                updateRoleInSupabase(userId, newRole, jwtToken);
            }
        } else {
            insertRoleInSupabase(userId, newRole, jwtToken);
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
        String url = "https://fexiivjyzplakakkiyqm.supabase.co/rest/v1/user_roles?user_id=eq." + userId;
        String body = String.format("{\"role\": \"%s\"}", newRole.name());
        sendSupabaseRequest(url, "PATCH", body, jwtToken, "actualizar", userId);
    }

    private void insertRoleInSupabase(UUID userId, AppRole newRole, String jwtToken) {
        String url = "https://fexiivjyzplakakkiyqm.supabase.co/rest/v1/user_roles";
        String body = String.format("{\"user_id\": \"%s\", \"role\": \"%s\"}", userId, newRole.name());
        sendSupabaseRequest(url, "POST", body, jwtToken, "insertar", userId);
    }

    private void sendSupabaseRequest(String url, String method, String body, String jwtToken, String action, UUID userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", supabaseProperties.getAnonKey())
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .method(method, HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseJson = objectMapper.readTree(response.body());

            String message = responseJson.has("message") ? responseJson.get("message").asText() : "Sin mensaje de error";

            if (statusCode >= 400) {
                throw new SupabaseException(statusCode, message);
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error de red al " + action + " el rol en Supabase para el usuario con ID: " + userId, e);
        } catch (SupabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al " + action + " el rol en Supabase para el usuario con ID: " + userId, e);
        }
    }

    // Nuevo método para eliminar el rol
    public void removeRole(UUID userId, String jwtToken) {
        if (jwtToken != null && !jwtToken.isEmpty()) {
            removeRoleWithHttp(userId, jwtToken);
        } else {
            removeRoleWithRepository(userId);
        }
    }

    private void removeRoleWithHttp(UUID userId, String jwtToken) {
        String url = "https://fexiivjyzplakakkiyqm.supabase.co/rest/v1/user_roles?user_id=eq." + userId;
        sendSupabaseRequest(url, "DELETE", "", jwtToken, "eliminar", userId);
    }

    private void removeRoleWithRepository(UUID userId) {
        UserRole role = userRoleRepository.findByUserId(userId);
        if (role == null) {
            throw new RuntimeException("No se encontró un rol para eliminar para el usuario con ID: " + userId);
        }
        userRoleRepository.delete(role);
    }
}
