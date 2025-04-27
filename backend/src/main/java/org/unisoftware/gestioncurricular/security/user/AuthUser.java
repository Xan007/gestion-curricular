package org.unisoftware.gestioncurricular.security.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthUser {
    private UUID id;
    private String email;
    private Instant createdAt;
}
