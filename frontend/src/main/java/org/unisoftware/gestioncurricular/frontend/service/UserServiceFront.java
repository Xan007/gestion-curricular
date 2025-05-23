package org.unisoftware.gestioncurricular.frontend.service;

import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.frontend.util.JwtDecodeUtil;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.unisoftware.gestioncurricular.frontend.dto.UserInfoDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceFront {

    public UserInfoDTO getCurrentUserInfo() {
        String token = SessionManager.getInstance().getToken();

        // Obtener datos desde el token
        String email = JwtDecodeUtil.getUsername(token);
        String role = JwtDecodeUtil.getRole(token);
        System.out.println(role);
        // Obtener el ID real del usuario autenticado usando el correo y la lista de usuarios
        String userId = null;
        try {
            URL url = new URL("http://localhost:8080/users?role=DOCENTE");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            try (InputStream in = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                List<UserInfoDTO> allUsers = mapper.readValue(in, new TypeReference<List<UserInfoDTO>>() {});
                for (UserInfoDTO user : allUsers) {
                    if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email) &&
                        user.getRole() != null && user.getRole().toUpperCase().contains("DOCENTE")) {
                        userId = user.getId();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("No se pudo obtener el userId por lista de usuarios: " + ex.getMessage());
        }

        // Guardar en sesión
        SessionManager.getInstance().setUserEmail(email);
        SessionManager.getInstance().setUserRole(role);
        SessionManager.getInstance().setUserId(userId);

        // Crear un objeto UserInfoDTO para devolver
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setEmail(email);
        userInfoDTO.setRole(role);
        userInfoDTO.setId(userId);

        return userInfoDTO;
    }

    public List<UserInfoDTO> getDocentes() throws Exception {
        // Obtener solo los usuarios con rol DOCENTE usando el endpoint filtrado
        URL url = new URL("http://localhost:8080/users?role=DOCENTE");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        List<UserInfoDTO> docentes;
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            docentes = mapper.readValue(in, new TypeReference<List<UserInfoDTO>>() {});
        }
        return docentes;
    }
}

