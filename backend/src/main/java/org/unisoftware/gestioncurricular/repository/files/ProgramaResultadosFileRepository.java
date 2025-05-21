package org.unisoftware.gestioncurricular.repository.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.files.ProgramaResultadosFile;

import java.util.List;
import java.util.Optional;

public interface ProgramaResultadosFileRepository extends JpaRepository<ProgramaResultadosFile, Long> {
    Optional<ProgramaResultadosFile> findFirstByProgramIdAndIsMainTrue(Long programId);

    List<ProgramaResultadosFile> findByProgramId(Long programId);
}
