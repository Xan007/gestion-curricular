package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "curso_por_programa")
@Getter
@Setter
public class CourseProgram {

    @EmbeddedId
    private CourseProgramId id;

    @ManyToOne
    @JoinColumn(name = "curso_id", insertable = false, updatable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "programa_id", insertable = false, updatable = false)
    private Program program;

    @Column(name = "semestre")
    private Integer semester;
}