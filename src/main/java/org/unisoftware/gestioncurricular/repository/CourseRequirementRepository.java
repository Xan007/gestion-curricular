package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.CourseRequirement;
import org.unisoftware.gestioncurricular.entity.CourseRequirementId;

import java.util.List;

public interface CourseRequirementRepository
        extends JpaRepository<CourseRequirement, CourseRequirementId> {

    /**
     * Elimina todos los requisitos asociados al programa dado.
     * Equivale a: DELETE FROM requisitos_cursos WHERE programa_id = :programId
     */
    void deleteById_ProgramId(Long programId);

    List<CourseRequirement> findById_ProgramIdAndId_CourseId(Long id, Long id1);

    List<CourseRequirement> findById_CourseId(Long courseId);
}
