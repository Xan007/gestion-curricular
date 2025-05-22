package org.unisoftware.gestioncurricular.repository.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.files.CursoMicrocurriculumFile;

import java.util.List;
import java.util.Optional;

public interface CursoMicrocurriculumFileRepository extends JpaRepository<CursoMicrocurriculumFile, Long> {
    Optional<CursoMicrocurriculumFile> findByCourseIdAndIsMainTrue(Long courseId);

    List<CursoMicrocurriculumFile> findByCourseId(Long courseId);
}
