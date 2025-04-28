package org.unisoftware.gestioncurricular.security.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;
import org.unisoftware.gestioncurricular.security.util.JwtUtil;
import org.unisoftware.gestioncurricular.security.role.UserRoleService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class SecurityFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final SupabaseProperties supabaseProperties;

    public SecurityFilter(JwtUtil jwtUtil, SupabaseProperties supabaseProperties) {
        this.jwtUtil = jwtUtil;
        this.supabaseProperties = supabaseProperties;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // Si es un token válido
            if (jwtUtil.isTokenValid(token)) {
                Claims claims = jwtUtil.extractAllClaims(token);

                // Verifica si el token corresponde al service_role_key
                String role = claims.get("role", String.class);  // Obtener el rol del claim 'role'

                if ("service_role".equals(role)) {
                    // Si es un 'service_role', asigna permisos de superusuario
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            null, null, List.of(new SimpleGrantedAuthority("ROLE_DECANO"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    // Caso para usuarios regulares
                    UUID userId = UUID.fromString(claims.getSubject());
                    String userRole = claims.get("user_role", String.class);
                    if (userRole == null) {
                        throw new ServletException("Role not found in token");
                    }

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole.toUpperCase());
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userId, null, List.of(authority)
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        chain.doFilter(request, response);
    }
}
