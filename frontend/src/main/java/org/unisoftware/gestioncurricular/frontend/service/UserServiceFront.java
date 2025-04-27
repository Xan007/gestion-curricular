package org.unisoftware.gestioncurricular.frontend.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.unisoftware.gestioncurricular.frontend.dto.UserInfoDTO;

@Service
public class UserServiceFront {
    // Endpoint actualizado
    private final String baseUrl = "http://localhost:8080/users/me/details";

    public UserInfoDTO getCurrentUserInfo() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + SessionManager.getInstance().getToken());
        headers.set("Accept", "application/hal+json"); // Sugerido por el endpoint

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<UserInfoDTO> response =
                restTemplate.exchange(baseUrl, HttpMethod.GET, entity, UserInfoDTO.class);

        UserInfoDTO userInfoDTO = response.getBody();
        // Guardamos datos relevantes en la sesión
        if (userInfoDTO != null) {
            SessionManager.getInstance().setUserEmail(userInfoDTO.getEmail());
            SessionManager.getInstance().setUserRoles(userInfoDTO.getRoles());
        }
        return userInfoDTO;
    }
}