package org.unisoftware.gestioncurricular.repository.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.files.ProgramaCurriculumFile;

import java.util.List;
import java.util.Optional;

public interface ProgramaCurriculumFileRepository extends JpaRepository<ProgramaCurriculumFile, Long> {
    Optional<ProgramaCurriculumFile> findFirstByProgramIdAndIsMainTrue(Long programId);

    List<ProgramaCurriculumFile> findByProgramId(Long programId);
}
