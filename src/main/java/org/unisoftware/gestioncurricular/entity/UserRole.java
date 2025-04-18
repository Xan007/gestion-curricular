package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.unisoftware.gestioncurricular.model.AppRole;

import java.net.ProtocolFamily;
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
