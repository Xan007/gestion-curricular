package org.unisoftware.gestioncurricular.security.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.security.util.JwtUtil;
import org.unisoftware.gestioncurricular.security.role.UserRoleService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SecurityFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final UserRoleService userRoleService;

    public SecurityFilter(JwtUtil jwtUtil,
                          UserRoleService userRoleService) {
        this.jwtUtil = jwtUtil;
        this.userRoleService = userRoleService;
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
            if (jwtUtil.isTokenValid(token)) {
                Claims claims = jwtUtil.extractAllClaims(token);

                // Obtengo el userId y el solo rol de una vez
                UUID userId = UUID.fromString(claims.getSubject());
                String role = claims.get("user_role", String.class);

                // Creo la autoridad y la asigno
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(authority)
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}
