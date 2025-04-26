package org.unisoftware.gestioncurricular.service;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unisoftware.gestioncurricular.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.entity.*;
import org.unisoftware.gestioncurricular.mapper.ProgramMapper;
import org.unisoftware.gestioncurricular.repository.*;
import org.unisoftware.gestioncurricular.util.studyPlanParser.PlanRow;
import org.unisoftware.gestioncurricular.util.studyPlanParser.StudyPlanParser;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgramService {

    @Autowired
    private EntityManager em;

    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final CourseProgramRepository courseProgramRepository;
    private final CourseRequirementRepository requirementRepository;
    private final ProgramMapper programMapper;
    private final List<StudyPlanParser> studyPlanParsers;
    private final StudyPlanViewRepository studyPlanViewRepository;

    public ProgramService(ProgramRepository programRepository,
                          CourseRepository courseRepository,
                          CourseProgramRepository courseProgramRepository,
                          CourseRequirementRepository requirementRepository,
                          ProgramMapper programMapper,
                          List<StudyPlanParser> studyPlanParsers,
                          StudyPlanViewRepository studyPlanViewRepository
    ) {
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.courseProgramRepository = courseProgramRepository;
        this.requirementRepository = requirementRepository;
        this.programMapper = programMapper;
        this.studyPlanParsers = studyPlanParsers;
        this.studyPlanViewRepository = studyPlanViewRepository;
    }

    @Transactional
    public Long createProgram(ProgramDTO dto) {
        Program p = programMapper.toEntity(dto);
        p = programRepository.save(p);
        return p.getId();
    }

    @Transactional(readOnly = true)
    public ProgramDTO getProgram(Long id) {
        Program p = programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Programa no encontrado: " + id));
        return programMapper.toDto(p);
    }

    @Transactional
    public void processStudyPlan(Long programId, List<PlanRow> planRows) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found: " + programId));

        courseProgramRepository.deleteById_ProgramId(programId);
        requirementRepository.deleteById_ProgramId(programId);

        for (PlanRow row : planRows) {
            Course course = courseRepository.findById(row.getSnies())
                    .orElseThrow(() -> new IllegalArgumentException("Course not found SNIES: " + row.getSnies()));

            // Guardar curso-programa
            CourseProgram cp = new CourseProgram();
            cp.setCourse(course);
            cp.setProgram(program);
            cp.setSemester(row.getSemester());
            courseProgramRepository.save(cp);

            System.out.println("Curso: " + course.getId() + " requisitos: " + row.getRequisitos());

            // Guardar requisitos SOLO si hay
            if (row.getRequisitos() != null && !row.getRequisitos().isEmpty()) {
                for (Long reqSnies : row.getRequisitos()) {
                    Course prereq = courseRepository.findById(reqSnies)
                            .orElseThrow(() -> new IllegalArgumentException("Prerequisite course not found SNIES: " + reqSnies));

                    CourseRequirement cr = new CourseRequirement();
                    CourseRequirementId crId = new CourseRequirementId();
                    crId.setCourseId(course.getId());
                    crId.setProgramId(programId);
                    crId.setPrerequisiteCourseId(prereq.getId());
                    cr.setId(crId);
                    cr.setCourse(course);
                    cr.setProgram(program);
                    cr.setPrerequisiteCourse(prereq);

                    requirementRepository.save(cr);
                }
            }
        }

        em.createNativeQuery("REFRESH MATERIALIZED VIEW study_plan_mv").executeUpdate();
    }

    @Transactional(readOnly = true)
    public List<StudyPlanEntry> getStudyPlan(Long programId) {
        programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found: " + programId));
        return studyPlanViewRepository.findById_ProgramIdOrderBySemesterAscId_CourseIdAsc(programId);
    }


    public ProgramDTO findProgramByName(String name) {
        Program program = programRepository.findByNameIgnoreCase(name)
                .orElse(null);
        return program != null ? programMapper.toDto(program) : null;
    }

    public List<ProgramDTO> getAllPrograms() {
        List<Program> programs = programRepository.findAll();
        return programs.stream()
                .map(programMapper::toDto)
                .collect(Collectors.toList());
    }



}