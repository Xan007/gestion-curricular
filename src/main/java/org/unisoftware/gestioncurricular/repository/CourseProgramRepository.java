package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.CourseProgram;

public interface CourseProgramRepository
        extends JpaRepository<CourseProgram, CourseProgram.CourseProgramId> {

    void deleteById_ProgramId(Long programId);
}
