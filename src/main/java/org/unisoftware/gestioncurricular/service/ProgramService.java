package org.unisoftware.gestioncurricular.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.unisoftware.gestioncurricular.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.entity.*;
import org.unisoftware.gestioncurricular.mapper.ProgramMapper;
import org.unisoftware.gestioncurricular.repository.CourseProgramRepository;
import org.unisoftware.gestioncurricular.repository.CourseRepository;
import org.unisoftware.gestioncurricular.repository.CourseRequirementRepository;
import org.unisoftware.gestioncurricular.repository.ProgramRepository;
import org.unisoftware.gestioncurricular.util.StudyPlanExcelParser;

import java.util.List;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final CourseProgramRepository courseProgramRepository;
    private final CourseRequirementRepository requirementRepository;
    private final ProgramMapper programMapper;

    public ProgramService(ProgramRepository programRepository,
                          CourseRepository courseRepository,
                          CourseProgramRepository courseProgramRepository,
                          CourseRequirementRepository requirementRepository,
                          ProgramMapper programMapper) {
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.courseProgramRepository = courseProgramRepository;
        this.requirementRepository = requirementRepository;
        this.programMapper = programMapper;
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
    public void uploadStudyPlan(Long programId, MultipartFile file) {
        // Verify program exists
        programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found: " + programId));

        // Clear existing plan and requirements for re-upload
        courseProgramRepository.deleteById_ProgramId(programId);
        requirementRepository.deleteById_ProgramId(programId);

        // Parse Excel
        List<StudyPlanExcelParser.PlanRow> planRows;
        try {
            planRows = StudyPlanExcelParser.parse(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file", e);
        }

        // Process each row
        for (var row : planRows) {
            // Find course by SNIES
            Course course = courseRepository.findById(row.getSnies())
                    .orElseThrow(() -> new IllegalArgumentException("Course not found SNIES: " + row.getSnies()));

            // Save CourseProgram entry
            CourseProgram cp = new CourseProgram();
            var cpId = new CourseProgram.CourseProgramId();
            cpId.setCourseId(course.getId());
            cpId.setProgramId(programId);
            cp.setId(cpId);
            cp.setSemester(row.getSemester());
            courseProgramRepository.save(cp);

            // Save each requirement
            for (Long reqSnies : row.getRequisitos()) {
                Course reqCourse = courseRepository.findById(reqSnies)
                        .orElseThrow(() -> new IllegalArgumentException("Prerequisite course not found SNIES: " + reqSnies));
                CourseRequirement req = new CourseRequirement();
                CourseRequirementId rid = new CourseRequirementId();
                rid.setCourseId(course.getId());
                rid.setProgramId(programId);
                req.setRequisitoCurso(reqCourse);
                req.setId(rid);
                requirementRepository.save(req);
            }
        }
    }
}