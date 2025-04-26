package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "requisitos_cursos")
@Getter
@Setter
public class CourseRequirement {

    @EmbeddedId
    private CourseRequirementId id;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "curso_id")
    private Course course;

    @ManyToOne
    @MapsId("programId")
    @JoinColumn(name = "programa_id")
    private Program program;

    @ManyToOne
    @MapsId("prerequisiteCourseId")
    @JoinColumn(name = "requisito_curso_id")
    private Course prerequisiteCourse;
}