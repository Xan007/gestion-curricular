package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.entity.AuthUser;

import java.util.UUID;

@Entity
@Table(name = "user_details")
@Getter
@Setter
public class UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Elimina la restricción unique de 'user_id' porque ya se manejará con la relación
    @Column(name = "user_id", nullable = false)
    private UUID userId;  // Mapea 'user_id' como la clave foránea

    @Column(name = "primer_nombre")
    private String primerNombre;

    @Column(name = "segundo_nombre")
    private String segundoNombre;

    @Column(name = "primer_apellido")
    private String primerApellido;

    @Column(name = "segundo_apellido")
    private String segundoApellido;

    // Relación One-to-One con AuthUser, sin necesidad de usar 'unique = true'
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private AuthUser authUser;
}
