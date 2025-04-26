package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.Course;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByNameIgnoreCase(String nombre);
}
