package org.unisoftware.gestioncurricular.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Acción de revisión sobre una propuesta (aceptar o rechazar)")
@Data
public class ProposalReviewRequest {

    @Schema(description = "Acción a realizar: ACCEPT o REJECT")
    private Action action;

    @Schema(description = "Observaciones del revisor", nullable = true)
    private String observations;

    public enum Action {
        ACCEPT,
        REJECT
    }
}