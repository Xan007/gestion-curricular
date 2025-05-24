package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class CourseProgramId implements Serializable {
    @Column(name = "curso_id")
    private Long courseId;

    @Column(name = "programa_id")
    private Long programId;

    @Column(name = "year")
    private Integer year;

    public CourseProgramId() {}

    public CourseProgramId(Long courseId, Long programId, Integer year) {
        this.courseId = courseId;
        this.programId = programId;
        this.year = year;
    }
}
