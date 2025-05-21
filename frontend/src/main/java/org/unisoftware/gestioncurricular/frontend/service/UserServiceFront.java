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

        // Guardar en sesión
        SessionManager.getInstance().setUserEmail(email);
        SessionManager.getInstance().setUserRole(role);

        // Crear un objeto UserInfoDTO para devolver
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setEmail(email);
        userInfoDTO.setRole(role);

        return userInfoDTO;
    }

    public List<UserInfoDTO> getDocentes() throws Exception {
        // 1. Obtener todos los usuarios (URL corregida)
        URL url = new URL("http://localhost:8080/users");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        List<UserInfoDTO> allUsers;
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            allUsers = mapper.readValue(in, new TypeReference<List<UserInfoDTO>>() {});
        }

        // 2. Filtrar solo los que tengan rol DOCENTE (puede ser lista de roles)
        List<UserInfoDTO> docentes = new ArrayList<>();
        for (UserInfoDTO user : allUsers) {
            URL roleUrl = new URL("http://localhost:8080/users/" + user.getId() + "/role");
            HttpURLConnection roleConn = (HttpURLConnection) roleUrl.openConnection();
            roleConn.setRequestMethod("GET");
            try (InputStream roleIn = roleConn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                // Puede ser un String o una lista de Strings
                try {
                    List<String> roles = mapper.readValue(roleIn, new TypeReference<List<String>>() {});
                    if (roles.contains("DOCENTE")) {
                        docentes.add(user);
                    }
                } catch (Exception ex) {
                    // Si no es lista, intentar como String
                    roleIn.close();
                    roleConn = (HttpURLConnection) roleUrl.openConnection();
                    roleConn.setRequestMethod("GET");
                    try (InputStream roleIn2 = roleConn.getInputStream()) {
                        String role = mapper.readValue(roleIn2, String.class);
                        if ("DOCENTE".equals(role)) {
                            docentes.add(user);
                        }
                    }
                }
            }
        }
        return docentes;
    }
}

