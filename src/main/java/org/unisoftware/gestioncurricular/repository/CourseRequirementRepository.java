package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.CourseRequirement;
import org.unisoftware.gestioncurricular.entity.CourseRequirementId;

public interface CourseRequirementRepository
        extends JpaRepository<CourseRequirement, CourseRequirementId> {

    /**
     * Elimina todos los requisitos asociados al programa dado.
     * Equivale a: DELETE FROM requisitos_cursos WHERE programa_id = :programId
     */
    void deleteById_ProgramId(Long programId);
}
