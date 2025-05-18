package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.unisoftware.gestioncurricular.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unisoftware.gestioncurricular.util.enums.ProposalStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long>, JpaSpecificationExecutor<Proposal> {
    List<Proposal> findByStatus(ProposalStatus estado);
    List<Proposal> findByTeacherId(UUID teacherId);
}
