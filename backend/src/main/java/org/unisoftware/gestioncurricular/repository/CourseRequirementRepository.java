package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.CourseRequirement;
import org.unisoftware.gestioncurricular.entity.CourseRequirementId;

import java.util.List;

public interface CourseRequirementRepository
        extends JpaRepository<CourseRequirement, CourseRequirementId> {
    void deleteById_ProgramId(Long programId);

    List<CourseRequirement> findById_ProgramIdAndId_CourseId(Long id, Long id1);

    List<CourseRequirement> findById_CourseId(Long courseId);

    List<CourseRequirement> findById_ProgramId(Long programId);

    void deleteByProgramIdAndId_Year(Long programId, Integer year);
}
