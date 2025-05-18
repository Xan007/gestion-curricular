package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.unisoftware.gestioncurricular.entity.AuthUser;
import org.unisoftware.gestioncurricular.util.enums.AppRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    @EntityGraph(attributePaths = {"userDetails", "userRole"})
    List<AuthUser> findAll();

    @EntityGraph(attributePaths = {"userDetails", "userRole"})
    Optional<AuthUser> findById(UUID id);

    @EntityGraph(attributePaths = {"userDetails", "userRole"})
    @Query("""
SELECT u FROM AuthUser u
LEFT JOIN FETCH u.userDetails d
LEFT JOIN FETCH u.userRole r
WHERE 
    (:email IS NULL OR :email = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
AND
    (:name IS NULL OR :name = '' OR (
        LOWER(d.primerNombre) LIKE LOWER(CONCAT('%', :name, '%'))
        OR LOWER(d.segundoNombre) LIKE LOWER(CONCAT('%', :name, '%'))
        OR LOWER(d.primerApellido) LIKE LOWER(CONCAT('%', :name, '%'))
        OR LOWER(d.segundoApellido) LIKE LOWER(CONCAT('%', :name, '%'))
    ))
AND
    (:role IS NULL OR r.role = :role)
""")
    List<AuthUser> searchByEmailNameAndRole(
            @Param("email") String email,
            @Param("name") String name,
            @Param("role") AppRole role
    );
}
