
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
import org.unisoftware.gestioncurricular.entity.Proposal;
import org.unisoftware.gestioncurricular.mapper.ProposalMapper;
import org.unisoftware.gestioncurricular.security.util.SecurityUtil;
import org.unisoftware.gestioncurricular.service.ProposalService;
import org.unisoftware.gestioncurricular.util.enums.ProposalStatus;

import java.util.List;
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
        // Implementación externa...
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
        Proposal updated = proposalService.reviewProposal(id, request.getAction(), request.getObservations(), role);
        return ResponseEntity.ok(proposalMapper.toDto(updated));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar propuestas", description = "Decano ve todas; director ve en_revision_director; comité ve en_revision_comite; docente ve sus propuestas.")
    @GetMapping
    public ResponseEntity<List<ProposalDTO>> listProposals() {
        String role = SecurityUtil.getCurrentUserRole();
        List<Proposal> proposals;
        UUID userId = SecurityUtil.getCurrentUserId();

        if ("ROLE_DECANO".equals(role)) {
            proposals = proposalService.getAllProposals();
        } else if ("ROLE_DIRECTOR_DE_PROGRAMA".equals(role)) {
            proposals = proposalService.getProposalsByStatus(ProposalStatus.EN_REVISION_DIRECTOR);
        } else if ("ROLE_COMITE_DE_PROGRAMA".equals(role)) {
            proposals = proposalService.getProposalsByStatus(ProposalStatus.EN_REVISION_COMITE);
        } else if ("ROLE_DOCENTE".equals(role)) {
            proposals = proposalService.getProposalsByTeacher(userId);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ProposalDTO> dtos = proposals.stream()
                .map(proposalMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}