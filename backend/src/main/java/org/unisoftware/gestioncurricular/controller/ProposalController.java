
package org.unisoftware.gestioncurricular.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.dto.ProposalDTO;
import org.unisoftware.gestioncurricular.dto.ProposalReviewRequest;
import org.unisoftware.gestioncurricular.dto.SignatureRequest;
import org.unisoftware.gestioncurricular.entity.Proposal;
import org.unisoftware.gestioncurricular.mapper.ProposalMapper;
import org.unisoftware.gestioncurricular.security.util.SecurityUtil;
import org.unisoftware.gestioncurricular.service.ProposalService;
import org.unisoftware.gestioncurricular.util.enums.ProposalStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones sobre propuestas académicas.
 */
@RestController
@RequestMapping("/proposals")
@RequiredArgsConstructor
@Tag(name = "Proposals", description = "Operaciones relacionadas con las propuestas académicas")
public class ProposalController {

    private final ProposalService proposalService;
    private final ProposalMapper proposalMapper;

    @Operation(summary = "Crear una nueva propuesta", description = "Crea una nueva propuesta académica.")
    @PostMapping
    public ResponseEntity<Void> createProposal(@RequestBody ProposalDTO dto) {

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PutMapping("/{id}/revisar")
    @Operation(summary = "Aceptar o rechazar una propuesta", description = "El director o comité pueden aceptar o rechazar una propuesta.")
    public ResponseEntity<ProposalDTO> reviewProposal(
            @Parameter(description = "ID de la propuesta") @PathVariable Long id,
            @RequestBody ProposalReviewRequest request
    ) {
        String role = SecurityUtil.getCurrentUserRole();
        if (role == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        UUID userId = SecurityUtil.getCurrentUserId();
        Proposal updated = proposalService.reviewProposal(id, request.getAction(), request.getObservations(), role, userId);
        return ResponseEntity.ok(proposalMapper.toDto(updated));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar propuestas", description = """
        Decano ve todas. 
        Director puede ver propuestas en revisión del director, del comité y esperando firmas. 
        Comité puede ver propuestas aceptadas, rechazadas y en revisión del comité.
        Docente solo puede ver sus propuestas.
        Se pueden filtrar por cursoId, docenteId y estado.
        """)
    @GetMapping
    public ResponseEntity<List<ProposalDTO>> listProposals(
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) UUID docenteId,
            @RequestParam(required = false) ProposalStatus estado
    ) {
        String role = SecurityUtil.getCurrentUserRole();
        UUID userId = SecurityUtil.getCurrentUserId();

        List<Proposal> proposals;

        if ("ROLE_DECANO".equals(role)) {
            proposals = proposalService.filterProposals(cursoId, docenteId, estado, null);
        } else if ("ROLE_DIRECTOR_DE_PROGRAMA".equals(role)) {
            Set<ProposalStatus> allowedStatuses = Set.of(
                    ProposalStatus.EN_REVISION_DIRECTOR,
                    ProposalStatus.EN_REVISION_COMITE,
                    ProposalStatus.ESPERANDO_FIRMAS
            );
            proposals = proposalService.filterProposals(cursoId, docenteId, estado, allowedStatuses);
        } else if ("ROLE_COMITE_DE_PROGRAMA".equals(role)) {
            Set<ProposalStatus> allowedStatuses = Set.of(
                    ProposalStatus.EN_REVISION_COMITE,
                    ProposalStatus.ACEPTADA,
                    ProposalStatus.RECHAZADA
            );
            proposals = proposalService.filterProposals(cursoId, docenteId, estado, allowedStatuses);
        } else if ("ROLE_DOCENTE".equals(role)) {
            proposals = proposalService.filterProposals(cursoId, userId, estado, null);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ProposalDTO> dtos = proposals.stream()
                .map(proposalMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasAnyRole('ROLE_DIRECTOR_DE_PROGRAMA', 'ROLE_DIRECTOR_DE_ESCUELA')")
    @Operation(summary = "Listar propuestas pendientes de firma", description = "Devuelve todas las propuestas en estado ESPERANDO_FIRMAS para que los directores puedan firmarlas.")
    @GetMapping("/pending-signatures")
    public ResponseEntity<List<ProposalDTO>> listProposalsPendingSignature() {
        String role = SecurityUtil.getCurrentUserRole();

        if (!"ROLE_DIRECTOR_DE_PROGRAMA".equals(role) && !"ROLE_DIRECTOR_DE_ESCUELA".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Proposal> proposals = proposalService.getProposalsByStatus(ProposalStatus.ESPERANDO_FIRMAS);

        List<ProposalDTO> dtos = proposals.stream()
                .map(proposalMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/sign")
    @PreAuthorize("hasAnyRole('ROLE_DIRECTOR_DE_PROGRAMA', 'ROLE_DIRECTOR_DE_ESCUELA')")
    @Operation(summary = "Firmar o rechazar una propuesta en estado esperando firmas")
    public ResponseEntity<ProposalDTO> signProposal(
            @PathVariable Long id,
            @RequestBody SignatureRequest request
    ) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String role = SecurityUtil.getCurrentUserRole();

        Proposal updated = proposalService.signProposal(id, userId, role, request.isAccept(), request.getObservations());
        return ResponseEntity.ok(proposalMapper.toDto(updated));
    }

}