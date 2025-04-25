package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class CourseRequirementId implements java.io.Serializable {

    @Column(name = "curso_id")
    private Long courseId;

    @Column(name = "programa_id")
    private Long programId;

    @Column(name = "requisito_curso_id")
    private Long requisitoCursoId;

    // equals() y hashCode(), nada más
}
