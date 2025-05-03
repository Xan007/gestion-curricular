package org.unisoftware.gestioncurricular.security.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;
import org.unisoftware.gestioncurricular.security.role.AppRole;
import org.unisoftware.gestioncurricular.security.role.UserRoleService;
import org.unisoftware.gestioncurricular.security.util.JwtUtil;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SupabaseProperties supabaseProperties;
    private final UserRoleService userRoleService;

    public SecurityFilter(JwtUtil jwtUtil, SupabaseProperties supabaseProperties, UserRoleService userRoleService) {
        this.jwtUtil = jwtUtil;
        this.supabaseProperties = supabaseProperties;
        this.userRoleService = userRoleService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            request.setAttribute("jwt", token);

            if (jwtUtil.isTokenValid(token)) {
                Claims claims = jwtUtil.extractAllClaims(token);
                String role = claims.get("role", String.class);

                if ("service_role".equals(role)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            null, null, List.of(new SimpleGrantedAuthority("ROLE_DECANO"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    UUID userId = UUID.fromString(claims.getSubject());

                    AppRole currentRole = userRoleService.getRoleForUser(userId);

                    if (currentRole == null) {
                        throw new ServletException("No roles found for user");
                    }

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + currentRole.name().toUpperCase());

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userId, null, List.of(authority)
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }



        filterChain.doFilter(request, response);
    }
}
