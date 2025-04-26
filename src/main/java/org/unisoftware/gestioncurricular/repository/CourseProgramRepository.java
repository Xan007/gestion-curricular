package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.unisoftware.gestioncurricular.dto.CoursePlanProjection;
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

    @Query(
            value = """
        SELECT
          c.id                            AS id,
          c.name                          AS name,
          c.tipo                          AS type,
          c.creditos                      AS credits,
          c.relacion                      AS relation,
          c.area                          AS area,
          c.ciclo                         AS cycle,
          cp.semestre                     AS semester,
          COALESCE(
            array_agg(cr.requisito_curso_id)
             FILTER (WHERE cr.requisito_curso_id IS NOT NULL),
            '{}'
          )                               AS requirements
        FROM curso_por_programa cp
        JOIN cursos c
          ON cp.curso_id = c.id
        LEFT JOIN requisitos_cursos cr
          ON cr.curso_id    = c.id
         AND cr.programa_id = :programId
        WHERE cp.programa_id = :programId
        GROUP BY
          c.id, c.name, c.tipo, c.creditos, c.relacion,
          c.area, c.ciclo, cp.semestre
        ORDER BY
          cp.semestre ASC,
          c.id        ASC
        """,
            nativeQuery = true
    )
    List<CoursePlanProjection> findStudyPlanByProgramId(Long programId);
}
