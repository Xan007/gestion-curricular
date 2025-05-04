package org.unisoftware.gestioncurricular.repository;

import org.unisoftware.gestioncurricular.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.unisoftware.gestioncurricular.util.enums.ProposalStatus;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByStatus(ProposalStatus estado);
}
