package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.unisoftware.gestioncurricular.entity.CourseProgram;
import org.unisoftware.gestioncurricular.entity.CourseProgramId;

import java.util.List;

public interface CourseProgramRepository
        extends JpaRepository<CourseProgram, CourseProgramId> {

    void deleteById_ProgramId(Long programId);

    List<CourseProgram> findById_ProgramId(Long programId);

    @Query("""
    SELECT cp
    FROM CourseProgram cp
    JOIN FETCH cp.course
    WHERE cp.id.programId = :programId
""")
    List<CourseProgram> findByProgramIdWithCourse(Long programId);
}
