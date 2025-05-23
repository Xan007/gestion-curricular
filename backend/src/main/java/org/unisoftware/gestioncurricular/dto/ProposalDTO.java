package org.unisoftware.gestioncurricular.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.dto.files.ProposalFileDTO;
import org.unisoftware.gestioncurricular.util.enums.ProposalStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Data Transfer Object para la entidad Proposal.
 */
@Getter
@Setter
@AllArgsConstructor
@Schema(name = "ProposalDTO", description = "Data Transfer Object for the Proposal entity.")
public class ProposalDTO {

    @Schema(description = "Identificador único de la propuesta")
    private Long id;

    @Schema(description = "Título de la propuesta")
    private String title;

    @Schema(description = "ID del curso asociado a la propuesta")
    private Long courseId;

    @Schema(description = "ID del docente que crea la propuesta")
    private UUID teacherId;

    /*@Schema(description = "ID del archivo subido en storage.objects")
    private UUID fileId;*/

    @Schema(description = "Estado actual de la propuesta")
    private ProposalStatus status;

    @Schema(description = "Observaciones o comentarios del director o comité")
    private String observations;

    @Schema(description = "Fecha y hora de creación de la propuesta")
    private Instant createdAt;

    @Schema(description = "Fecha y hora de la última actualización")
    private Instant lastUpdatedAt;

    @Schema(description = "Indica si la propuesta ha sido firmada por el director del programa")
    private boolean signedByDirectorPrograma;

    @Schema(description = "Indica si la propuesta ha sido firmada por el director de escuela")
    private boolean signedByDirectorEscuela;

    @Schema(description = "Indica si el docente puede editar la propuesta")
    private boolean canEdit = true;

    @Schema(description = "ID del archivo de la propuesta")
    private ProposalFileDTO file;
}