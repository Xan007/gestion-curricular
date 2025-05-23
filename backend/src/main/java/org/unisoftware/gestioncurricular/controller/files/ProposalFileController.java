// ProposalFileController.java
package org.unisoftware.gestioncurricular.controller.files;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.dto.files.ProposalFileDTO;
import org.unisoftware.gestioncurricular.entity.Proposal;
import org.unisoftware.gestioncurricular.repository.ProposalRepository;
import org.unisoftware.gestioncurricular.security.util.SecurityUtil;
import org.unisoftware.gestioncurricular.service.files.ProposalFileService;

@RestController
@RequestMapping("/proposals/{courseId}/files")
@Tag(name = "Proposal Files", description = "Gestión de archivos asociados a propuestas académicas")
public class ProposalFileController {

    private final ProposalFileService proposalFileService;
    private final ProposalRepository proposalRepository;

    public ProposalFileController(ProposalFileService proposalFileService,
                                  ProposalRepository proposalRepository) {
        this.proposalFileService = proposalFileService;
        this.proposalRepository = proposalRepository;
    }

    @Operation(
            summary = "Generar URL para subir archivo de propuesta",
            description = "Genera una URL prefirmada para que un docente suba un archivo asociado a una propuesta. Requiere rol DOCENTE, que sea propietario y que la propuesta pueda ser editada.",
            parameters = {
                    @Parameter(name = "courseId", description = "ID del curso asociado", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "proposalId", description = "ID de la propuesta", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "URL generada exitosamente"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado por no ser propietario o porque no se puede editar"),
                    @ApiResponse(responseCode = "404", description = "Propuesta no encontrada")
            }
    )
    @PreAuthorize("hasRole('DOCENTE')")
    @GetMapping("/{proposalId}/upload-url")
    public ResponseEntity<?> getUploadUrl(
            @PathVariable Long courseId,
            @PathVariable Long proposalId) {
        var currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        if (!proposal.getTeacherId().equals(currentUserId)) {
            return ResponseEntity.status(403).body("User is not the owner of the proposal");
        }

        ProposalFileDTO dto = proposalFileService.generateUploadUrl(proposalId);
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Obtener URL del archivo de propuesta",
            description = "Devuelve la URL pública del archivo asociado a una propuesta. Requiere rol docente, director, comité o decano.",
            parameters = {
                    @Parameter(name = "courseId", description = "ID del curso asociado", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "proposalId", description = "ID de la propuesta", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "URL obtenida correctamente"),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @GetMapping("/{proposalId}")
    @PreAuthorize("hasAnyRole('DOCENTE','DIRECTOR_DE_PROGRAMA','DIRECTOR_DE_ESCUELA','COMITE_DE_PROGRAMA','DECANO')")
    public ResponseEntity<?> getProposalFileUrl(
            @PathVariable Long courseId,
            @PathVariable Long proposalId) {
        var currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        if (!proposal.getCourse().getId().equals(courseId)) {
            return ResponseEntity.badRequest().body("Proposal does not belong to course");
        }

        String currentUserRole = SecurityUtil.getCurrentUserRole();

        if ("ROLE_DOCENTE".equals(currentUserRole) && !proposal.getTeacherId().equals(currentUserId)) {
            return ResponseEntity.status(403).body("User is not the owner of the proposal");
        }

        try {
            ProposalFileDTO dto = proposalFileService.getProposalFileUrl(proposalId, courseId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
