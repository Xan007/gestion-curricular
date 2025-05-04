package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.unisoftware.gestioncurricular.util.enums.AppRole;
import org.unisoftware.gestioncurricular.entity.converters.AppRoleConverter;

import java.util.UUID;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Convert(converter = AppRoleConverter.class)
    @Column(
            name = "role",
            nullable = false,
            columnDefinition = "app_role"
    )
    @ColumnTransformer(
            read  = "CAST(role AS VARCHAR)",
            write = "?::app_role"
    )
    private AppRole role;
}
