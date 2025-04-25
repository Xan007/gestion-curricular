package org.unisoftware.gestioncurricular.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unisoftware.gestioncurricular.dto.CourseDTO;
import org.unisoftware.gestioncurricular.entity.Course;
import org.unisoftware.gestioncurricular.mapper.CourseMapper;
import org.unisoftware.gestioncurricular.repository.CourseRepository;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository,
                         CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    @Transactional
    public void processCourses(List<CourseDTO> courseDTOs) {
        for (CourseDTO dto : courseDTOs) {
            Optional<Course> optCourse = courseRepository.findById(dto.getId());
            if (optCourse.isPresent()) {
                Course existing = optCourse.get();
                // MapStruct will update all fields except id & createdAt
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
}