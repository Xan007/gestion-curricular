package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.CourseProgram;
import org.unisoftware.gestioncurricular.entity.CourseProgramId;

import java.util.List;

public interface CourseProgramRepository
        extends JpaRepository<CourseProgram, CourseProgramId> {

    void deleteById_ProgramId(Long programId);

    List<CourseProgram> findById_ProgramId(Long programId);
}
