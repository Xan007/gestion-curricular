package org.unisoftware.gestioncurricular.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
}