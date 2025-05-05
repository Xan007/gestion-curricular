package org.unisoftware.gestioncurricular.frontend.service;

import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.frontend.util.JwtDecodeUtil;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.unisoftware.gestioncurricular.frontend.dto.UserInfoDTO;

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
}
