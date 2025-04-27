package org.unisoftware.gestioncurricular.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AuthUserRepository {
    private final JdbcTemplate jdbcTemplate;

    public AuthUser findById(UUID userId) {
        return jdbcTemplate.queryForObject(
                "SELECT id, email, created_at FROM auth.users WHERE id = ?",
                (rs, rowNum) -> new AuthUser(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("email"),
                        rs.getTimestamp("created_at").toInstant()
                ),
                userId
        );
    }

    public List<AuthUser> findAll() {
        return jdbcTemplate.query(
                "SELECT id, email, created_at FROM auth.users",
                (rs, rowNum) -> new AuthUser(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("email"),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
    }
}
