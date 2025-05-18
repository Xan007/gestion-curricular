// src/main/java/org/unisoftware/gestioncurricular/entity/Proposal.java
package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.unisoftware.gestioncurricular.entity.converters.ProposalStatusConverter;
import org.unisoftware.gestioncurricular.util.enums.ProposalStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "propuestas", schema = "public")
@Getter
@Setter
public class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false)
    private String title;

    @Column(name = "curso_id", nullable = false)
    private Long courseId;

    @Column(name = "docente_id", nullable = false)
    private UUID teacherId;

    @Column(name = "archivo_id")
    private UUID fileId;

    /**
     * Mapea el enum Java ProposalStatus al tipo estado_propuesta de PostgreSQL.
     */
    @Column(name = "estado", nullable = false, columnDefinition = "estado_propuesta")
    @ColumnTransformer(
            read  = "CAST(estado AS VARCHAR)",
            write = "?::estado_propuesta"
    )
    @Convert(converter = ProposalStatusConverter.class)
    private ProposalStatus status = ProposalStatus.EN_REVISION_DIRECTOR;

    @Column(name = "observaciones")
    private String observations;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt = Instant.now();

    // getters y setters...

    @Setter
    @Getter
    @Column(name = "signed_by_director_programa", nullable = false)
    private boolean signedByDirectorPrograma = false;

    @Setter
    @Getter
    @Column(name = "signed_by_director_escuela", nullable = false)
    private boolean signedByDirectorEscuela = false;

    @Setter
    @Getter
    @Column(name = "signature_rejected", nullable = false)
    private boolean signatureRejected = false;
}
