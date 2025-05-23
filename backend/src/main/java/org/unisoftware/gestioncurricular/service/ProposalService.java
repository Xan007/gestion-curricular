package org.unisoftware.gestioncurricular.service;

import org.springframework.data.jpa.domain.Specification;
import org.unisoftware.gestioncurricular.dto.ProposalReviewRequest;
import org.unisoftware.gestioncurricular.dto.ProposalUploadRequest;
import org.unisoftware.gestioncurricular.entity.Course;
import org.unisoftware.gestioncurricular.entity.Proposal;
import org.unisoftware.gestioncurricular.repository.CourseRepository;
import org.unisoftware.gestioncurricular.repository.ProposalRepository;
import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.util.enums.ProposalStatus;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio para gestionar la lógica de negocio de las propuestas.
 */
@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final NotificationService notificationService;
    private final CourseRepository courseRepository;

    public ProposalService(ProposalRepository proposalRepository, NotificationService notificationService, CourseRepository courseRepository) {
        this.proposalRepository = proposalRepository;
        this.notificationService = notificationService;
        this.courseRepository = courseRepository;
    }

    public List<Proposal> getAllProposals() {
        return proposalRepository.findAll();
    }

    public List<Proposal> getProposalsByStatus(ProposalStatus status) {
        return proposalRepository.findByStatus(status);
    }

    public List<Proposal> getProposalsByTeacher(UUID teacherId) {
        return proposalRepository.findByTeacherId(teacherId);
    }

    public Proposal reviewProposal(Long id, ProposalReviewRequest.Action action, String observations, String role, UUID userId) {
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
                    proposal.setCanEdit(false);
                } else if (action == ProposalReviewRequest.Action.REJECT) {
                    proposal.setStatus(ProposalStatus.AJUSTES_SOLICITADOS);
                    proposal.setCanEdit(true);
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

        String actionLabel = switch (action) {
            case ACCEPT -> "ACEPTAR";
            case REJECT -> currentStatus == ProposalStatus.EN_REVISION_DIRECTOR ? "AJUSTES" : "RECHAZAR";
        };

        String updatedObservations = appendObservation(proposal.getObservations(), role, userId, actionLabel, observations);
        proposal.setObservations(updatedObservations);
        proposal.setLastUpdatedAt(Instant.now());

        String notifTitle = "Tu propuesta ha sido revisada";
        String notifBody = String.format(
                "%s por %s en propuesta [ID: %d, Título: %s]%s",
                actionLabel,
                role.replace("ROLE_", ""),
                proposal.getId(),
                proposal.getTitle(),
                (observations != null && !observations.isBlank() ? ": " + observations : "")
        );
        notificationService.sendNotification(proposal.getTeacherId(), notifTitle, notifBody);

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

        boolean accepted = proposal.isSignedByDirectorPrograma() &&
                proposal.isSignedByDirectorEscuela() &&
                !proposal.isSignatureRejected();

        if (accepted) {
            proposal.setStatus(ProposalStatus.ACEPTADA);

            notificationService.sendNotification(
                    proposal.getTeacherId(),
                    "Propuesta aceptada",
                    String.format(
                            "Tu propuesta [ID: %d, Título: %s] ha sido firmada por ambos directores y ha sido aceptada.",
                            proposal.getId(),
                            proposal.getTitle()
                    )
            );
        } else if (proposal.isSignatureRejected()) {
            notificationService.sendNotification(
                    proposal.getTeacherId(),
                    "Propuesta rechazada",
                    String.format(
                            "%s rechazó la propuesta [ID: %d, Título: %s]%s",
                            role.replace("ROLE_", ""),
                            proposal.getId(),
                            proposal.getTitle(),
                            (observations != null && !observations.isBlank() ? ": " + observations : ".")
                    )
            );
        }

        String actionLabel = "FIRMA_" + (accept ? "OK" : "NO");
        String updatedObs = appendObservation(proposal.getObservations(), role, signerId, actionLabel, observations);
        proposal.setObservations(updatedObs);
        proposal.setLastUpdatedAt(Instant.now());

        return proposalRepository.save(proposal);
    }

    private String appendObservation(String existing, String role, UUID userId, String actionLabel, String note) {
        String roleClean = role.replace("ROLE_", "");
        String timestamp = Instant.now().toString().substring(0, 16); // yyyy-MM-ddTHH:mm
        String cleanNote = note == null ? "" : note.replaceAll("\\R+", " ").trim();
        String newEntry = String.join("|", timestamp, roleClean, userId.toString(), actionLabel, cleanNote);

        return (existing == null || existing.isBlank())
                ? newEntry
                : existing + "\n" + newEntry;
    }

    public List<Proposal> filterProposals(Long cursoId, UUID docenteId, ProposalStatus estado, Set<ProposalStatus> allowedStatuses) {
        Specification<Proposal> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cursoId != null) {
                predicates.add(cb.equal(root.get("course").get("id"), cursoId));
            }

            if (docenteId != null) {
                predicates.add(cb.equal(root.get("teacherId"), docenteId));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("status"), estado));
            }

            if (allowedStatuses != null && !allowedStatuses.isEmpty()) {
                predicates.add(root.get("status").in(allowedStatuses));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return proposalRepository.findAll(spec);
    }

    public Proposal createProposal(ProposalUploadRequest request, UUID teacherId) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + request.getCourseId()));

        Proposal proposal = new Proposal();
        proposal.setTitle(request.getTitle());
        proposal.setCourse(course);
        proposal.setTeacherId(teacherId);

        proposal.setStatus(ProposalStatus.EN_REVISION_DIRECTOR);
        proposal.setCreatedAt(Instant.now());
        proposal.setLastUpdatedAt(Instant.now());
        proposal.setCanEdit(true);

        return proposalRepository.save(proposal);
    }
}
