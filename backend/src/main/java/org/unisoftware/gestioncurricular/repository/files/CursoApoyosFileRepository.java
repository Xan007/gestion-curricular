package org.unisoftware.gestioncurricular.repository.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.files.CursoApoyosFile;
import org.unisoftware.gestioncurricular.util.enums.AcademicSupportType;

import java.util.List;
import java.util.UUID;

public interface CursoApoyosFileRepository extends JpaRepository<CursoApoyosFile, Long> {
    List<CursoApoyosFile> findByCourseIdAndTipo(Long courseId, AcademicSupportType t);

    List<CursoApoyosFile> findAllByCourseId(Long courseId);

    CursoApoyosFile findByCourseIdAndFileId(Long courseId, UUID fileId);
}
