package org.unisoftware.gestioncurricular.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.unisoftware.gestioncurricular.service.UserRoleService;
import org.unisoftware.gestioncurricular.util.JwtUtil;

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
                UUID userId = UUID.fromString(claims.getSubject());

                // Carga roles desde la BD
                List<String> roles = userRoleService.getRolesForUser(userId);

                // Convierte a GrantedAuthority
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                        .collect(Collectors.toList());

                // Autentica
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}
