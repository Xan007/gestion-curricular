package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "curso_por_programa")
@Setter
@Getter
public class CourseProgram {

    @EmbeddedId
    private CourseProgramId id;

    @Column(name = "semestre")
    private Integer semester;

    @Embeddable
    @Setter
    @Getter
    public static class CourseProgramId implements java.io.Serializable {
        @Column(name = "curso_id")
        private Long courseId;

        @Column(name = "programa_id")
        private Long programId;

        // Getters and Setters, equals() y hashCode()
    }

    // Getters and Setters
}
