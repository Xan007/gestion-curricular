package org.unisoftware.gestioncurricular.service;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.unisoftware.gestioncurricular.dto.ProposalReviewRequest;
import org.unisoftware.gestioncurricular.entity.Proposal;
import org.unisoftware.gestioncurricular.repository.ProposalRepository;
import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.util.enums.ProposalStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar la lógica de negocio de las propuestas.
 */
@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;

    public ProposalService(ProposalRepository proposalRepository) {
        this.proposalRepository = proposalRepository;
    }

    /**
     * Obtiene todas las propuestas.
     */
    public List<Proposal> getAllProposals() {
        return proposalRepository.findAll();
    }

    /**
     * Obtiene las propuestas por estado.
     */
    public List<Proposal> getProposalsByStatus(ProposalStatus status) {
        return proposalRepository.findByStatus(status);
    }

    public List<Proposal> getProposalsByTeacher(UUID teacherId) {
        return proposalRepository.findByTeacherId(teacherId);
    }


    public Proposal reviewProposal(Long id, ProposalReviewRequest.Action action, String observations, String role) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada: " + id));

        ProposalStatus currentStatus = proposal.getStatus();

        switch (role) {
            case "ROLE_DIRECTOR_DE_PROGRAMA":
                if (currentStatus != ProposalStatus.EN_REVISION_DIRECTOR) {
                    throw new IllegalStateException("La propuesta no está en revisión del director");
                }

                if (action == ProposalReviewRequest.Action.ACCEPT) {
                    proposal.setStatus(ProposalStatus.EN_REVISION_COMITE);
                } else if (action == ProposalReviewRequest.Action.REJECT) {
                    proposal.setStatus(ProposalStatus.AJUSTES_SOLICITADOS);
                } else {
                    throw new IllegalArgumentException("Acción no válida para el director");
                }
                break;

            case "ROLE_COMITE_DE_PROGRAMA":
                if (currentStatus != ProposalStatus.EN_REVISION_COMITE) {
                    throw new IllegalStateException("La propuesta no está en revisión del comité");
                }

                if (action == ProposalReviewRequest.Action.ACCEPT) {
                    proposal.setStatus(ProposalStatus.ESPERANDO_FIRMAS);
                } else if (action == ProposalReviewRequest.Action.REJECT) {
                    proposal.setStatus(ProposalStatus.RECHAZADA);
                } else {
                    throw new IllegalArgumentException("Acción no válida para el comité");
                }
                break;

            default:
                throw new SecurityException("Rol no autorizado para esta operación");
        }

        // Acumular observaciones
        String existing = proposal.getObservations();
        String prefix = "[" + Instant.now() + " - " + role + " - " + action.name() + "]";
        String newEntry = prefix + (observations != null && !observations.isBlank() ? " " + observations : "");
        String updatedObservations = (existing == null || existing.isBlank())
                ? newEntry
                : existing + "\n" + newEntry;

        proposal.setObservations(updatedObservations);
        proposal.setLastUpdatedAt(Instant.now());

        return proposalRepository.save(proposal);
    }

    public Proposal signProposal(Long proposalId, UUID signerId, String role, boolean accept, String observations) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Propuesta no encontrada: " + proposalId));

        if (proposal.getStatus() != ProposalStatus.ESPERANDO_FIRMAS) {
            throw new IllegalStateException("La propuesta no está en estado de espera de firmas");
        }

        switch (role) {
            case "ROLE_DIRECTOR_DE_PROGRAMA":
                if (proposal.isSignedByDirectorPrograma()) {
                    throw new IllegalStateException("Director de programa ya firmó");
                }
                if (accept) {
                    proposal.setSignedByDirectorPrograma(true);
                } else {
                    proposal.setSignatureRejected(true);
                    proposal.setStatus(ProposalStatus.RECHAZADA);
                }
                break;

            case "ROLE_DIRECTOR_DE_ESCUELA":
                if (proposal.isSignedByDirectorEscuela()) {
                    throw new IllegalStateException("Director de escuela ya firmó");
                }
                if (accept) {
                    proposal.setSignedByDirectorEscuela(true);
                } else {
                    proposal.setSignatureRejected(true);
                    proposal.setStatus(ProposalStatus.RECHAZADA);
                }
                break;

            default:
                throw new SecurityException("Rol no autorizado para firmar");
        }

        // Si ambos firmaron y no hay rechazo, se acepta la propuesta
        if (proposal.isSignedByDirectorPrograma() && proposal.isSignedByDirectorEscuela() && !proposal.isSignatureRejected()) {
            proposal.setStatus(ProposalStatus.ACEPTADA);
        }

        // Guardar observaciones con prefijo de firma
        String existingObs = proposal.getObservations();
        String prefix = "[" + Instant.now() + " - " + role + " - SIGNATURE " + (accept ? "ACCEPTED" : "REJECTED") + "]";
        String newEntry = prefix + (observations != null && !observations.isBlank() ? " " + observations : "");
        String updatedObs = (existingObs == null || existingObs.isBlank()) ? newEntry : existingObs + "\n" + newEntry;
        proposal.setObservations(updatedObs);

        proposal.setLastUpdatedAt(Instant.now());

        return proposalRepository.save(proposal);
    }

}