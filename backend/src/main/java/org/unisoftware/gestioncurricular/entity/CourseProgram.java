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
    private CourseProgramId id = new CourseProgramId();

    @Column(name = "semestre")
    private Integer semester;

    @ManyToOne
    @MapsId("courseId")
    @JoinColumn(name = "curso_id")
    private Course course;

    @ManyToOne
    @MapsId("programId")
    @JoinColumn(name = "programa_id")
    private Program program;
}
