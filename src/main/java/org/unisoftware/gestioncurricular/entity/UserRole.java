package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.unisoftware.gestioncurricular.security.role.AppRole;

import java.util.UUID;

@Entity
@Table(name = "user_roles")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    @Getter
    private AppRole role;

    // getters y setters
    // …
}
