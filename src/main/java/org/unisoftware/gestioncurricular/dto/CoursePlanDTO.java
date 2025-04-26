package org.unisoftware.gestioncurricular.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing a course within a study plan, including all course attributes,
 * the semester, and nested prerequisites if any.
 */
@Getter
@Setter
public class CoursePlanDTO {
    private Long id;
    private String name;
    private Long microcurriculumFileId;
    private String type;
    private Integer credits;
    private String relation;
    private String area;
    private String teacherId;
    private String cycle;
    private Integer semester;
    private List<Long> requirements;
}