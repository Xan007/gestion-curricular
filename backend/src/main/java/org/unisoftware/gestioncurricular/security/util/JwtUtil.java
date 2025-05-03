package org.unisoftware.gestioncurricular.security.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;

import java.security.Key;
import java.util.Map;

@Component
public class JwtUtil {

    private final SupabaseProperties properties;
    private Key secretKey;

    // Constructor
    public JwtUtil(SupabaseProperties properties) {
        this.properties = properties;
    }

    // Inicialización del secretKey al inicio de la aplicación
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes());
    }

    // Extraer todos los claims del token
    public Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Validar si el token es válido
    public boolean isTokenValid(String token) {
        try {
            // Intentar extraer los claims del token para validarlo
            extractAllClaims(token);
            return true;  // Si no lanza excepciones, el token es válido
        } catch (JwtException e) {
            return false;  // Si hubo error, el token no es válido
        }
    }

    // Extraer el nombre de usuario (subject) del token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extraer el rol del usuario desde el token
    public String extractUserRole(String token) {
        return extractAllClaims(token).get("user_role", String.class);
    }

    // Extraer claims personalizados desde el token
    public Map<String, Object> extractCustomClaims(String token) {
        return extractAllClaims(token);
    }


}
