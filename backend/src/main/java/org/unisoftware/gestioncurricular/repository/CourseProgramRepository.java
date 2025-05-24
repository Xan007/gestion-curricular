package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    void deleteById_ProgramIdAndId_Year(Long programId, Integer year);

    @Query("SELECT MAX(cp.id.year) FROM CourseProgram cp WHERE cp.id.programId = :programId")
    Integer findLatestYearByProgramId(@Param("programId") Long programId);

    List<CourseProgram> findById_ProgramIdAndId_Year(Long programId, Integer year);

    @Query("SELECT DISTINCT cp.id.year FROM CourseProgram cp WHERE cp.id.programId = :programId ORDER BY cp.id.year DESC")
    List<Integer> findDistinctYearsByProgramIdOrderByYearDesc(@Param("programId") Long programId);
}
