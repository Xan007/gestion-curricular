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
    @JoinColumn(name = "curso_id", insertable = false, updatable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "requisito_curso_id", insertable = false, updatable = false)
    private Course requisitoCurso;
}
