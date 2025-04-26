package org.unisoftware.gestioncurricular.entity;

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
    private Long courseId;
    private Long programId;
}
