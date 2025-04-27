package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;

@Embeddable
@Getter
public class StudyPlanEntryId implements Serializable {
    @Column(name="program_id")
    private Long programId;
    @Column(name="course_id")
    private Long courseId;
}
