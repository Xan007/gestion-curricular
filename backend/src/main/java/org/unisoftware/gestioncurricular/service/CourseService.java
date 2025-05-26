package org.unisoftware.gestioncurricular.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unisoftware.gestioncurricular.dto.CourseDTO;
import org.unisoftware.gestioncurricular.entity.Course;
import org.unisoftware.gestioncurricular.entity.CourseProgram;
import org.unisoftware.gestioncurricular.entity.CourseRequirement;
import org.unisoftware.gestioncurricular.mapper.CourseMapper;
import org.unisoftware.gestioncurricular.repository.CourseProgramRepository;
import org.unisoftware.gestioncurricular.repository.CourseRepository;
import org.unisoftware.gestioncurricular.repository.CourseRequirementRepository;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseProgramRepository courseProgramRepository;
    @Autowired
    private EntityManager em;

    public CourseService(CourseRepository courseRepository,
                         CourseMapper courseMapper, CourseProgramRepository courseProgramRepository) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.courseProgramRepository = courseProgramRepository;
    }

    @Transactional
    public void processCourses(List<CourseDTO> courseDTOs) {
        for (CourseDTO dto : courseDTOs) {
            Optional<Course> optCourse = courseRepository.findById(dto.getId());
            if (optCourse.isPresent()) {
                Course existing = optCourse.get();
                courseMapper.updateCourseFromDto(dto, existing);
                courseRepository.save(existing);
            } else {
                Course newCourse = courseMapper.toEntity(dto);
                newCourse.setId(dto.getId());
                newCourse.setCreatedAt(LocalDateTime.now());
                courseRepository.save(newCourse);
            }
        }
    }

    @Transactional(readOnly = true)
    public CourseDTO getCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        return courseMapper.toDto(course);
    }

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toDto)
                .toList();
    }

    public CourseDTO getCourseByName(String nombre) {
        Course course = courseRepository.findByNameIgnoreCase(nombre)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + nombre));

        return courseMapper.toDto(course);
    }

    public List<CourseDTO> getCoursesByProgramId(Long programId) {
        List<CourseProgram> coursePrograms = courseProgramRepository.findByProgramIdWithCourse(programId);
        return coursePrograms.stream()
                .map(CourseProgram::getCourse)
                .map(courseMapper::toDto)
                .toList();
    }

    @Transactional
    public CourseDTO updateCourse(Long courseId, CourseDTO dto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        // Actualiza campos del curso con los datos del DTO
        courseMapper.updateCourseFromDto(dto, course);
        // No cambiamos createdAt para preservar la fecha original
        course = courseRepository.save(course);

        em.createNativeQuery("REFRESH MATERIALIZED VIEW study_plan_mv").executeUpdate();

        return courseMapper.toDto(course);
    }

    @Transactional
    public CourseDTO assignTeacherToCourse(Long courseId, UUID docenteId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        course.setTeacherId(docenteId);
        Course saved = courseRepository.save(course);
        return courseMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByDocenteId(UUID docenteId) {
        List<Course> courses = courseRepository.findByTeacherId(docenteId);
        return courses.stream().map(courseMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public boolean isDocenteOwnerOfCourse(Long courseId, UUID docenteId) {
        return courseRepository.findById(courseId)
                .map(course -> docenteId != null && docenteId.equals(course.getTeacherId()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByProgramAndSemester(Long programId, Integer year, Integer semester) {
        List<CourseProgram> coursePrograms = courseProgramRepository.findById_ProgramIdAndId_Year(programId, year).stream()
                .filter(cp -> cp.getSemester() != null && cp.getSemester().equals(semester))
                .toList();
        return coursePrograms.stream()
                .map(CourseProgram::getCourse)
                .map(courseMapper::toDto)
                .toList();
    }
}