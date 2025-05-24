package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class CourseRequirementId implements Serializable {

    @Column(name = "curso_id")
    private Long courseId;

    @Column(name = "programa_id")
    private Long programId;

    @Column(name = "requisito_curso_id")
    private Long prerequisiteCourseId;

    @Column(name = "year")
    private Integer year;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseRequirementId)) return false;
        CourseRequirementId that = (CourseRequirementId) o;
        return Objects.equals(courseId, that.courseId) &&
                Objects.equals(programId, that.programId) &&
                Objects.equals(prerequisiteCourseId, that.prerequisiteCourseId)
                && Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, programId, prerequisiteCourseId, year);
    }
}

