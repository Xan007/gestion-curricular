package org.unisoftware.gestioncurricular.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.unisoftware.gestioncurricular.entity.Course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByNameIgnoreCase(String nombre);

    List<Course> findByTeacherId(UUID docenteId);
}
