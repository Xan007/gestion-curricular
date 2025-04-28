package org.unisoftware.gestioncurricular.frontend.util;

import java.util.Base64;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JwtDecodeUtil {

    // Devuelve un Map con los claims del token (no valida la firma)
    public static Map<String, Object> decode(String jwtToken) {
        if (jwtToken == null || jwtToken.isEmpty()) return Collections.emptyMap();
        String[] parts = jwtToken.split("\\.");
        if (parts.length < 2) return Collections.emptyMap();
        try {
            String payload = parts[1];
            String json = new String(Base64.getUrlDecoder().decode(payload));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    // Para obtener el nombre de usuario (por defecto va en "sub" o "email" o similar según tu JWT)
    public static String getUsername(String jwtToken) {
        Map<String, Object> claims = decode(jwtToken);
        if (claims.containsKey("username")) return claims.get("username").toString();
        if (claims.containsKey("email")) return claims.get("email").toString();
        if (claims.containsKey("sub")) return claims.get("sub").toString();
        return "";
    }

    public static List<String> getRoles(String jwtToken) {
        Map<String, Object> claims = decode(jwtToken);

        if (claims.containsKey("user_role")) {
            Object r = claims.get("user_role");
            return r instanceof String ? List.of((String) r) : Collections.emptyList();
        }

        return Collections.emptyList();
    }

}