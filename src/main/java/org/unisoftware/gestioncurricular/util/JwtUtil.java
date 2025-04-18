package org.unisoftware.gestioncurricular.util;

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

    public JwtUtil(SupabaseProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes());
    }

    public Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUserRole(String token) {
        return extractAllClaims(token).get("user_role", String.class);
    }

    public Map<String, Object> extractCustomClaims(String token) {
        return extractAllClaims(token);
    }
}
