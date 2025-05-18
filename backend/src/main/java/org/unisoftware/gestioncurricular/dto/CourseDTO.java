package org.unisoftware.gestioncurricular.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseArea;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseCycle;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseType;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String name;
    private CourseType type;
    private CourseCycle cycle;
    private CourseArea area;
    private Integer credits;
    private String relation;

    private Long microcurriculumFileId;
    private UUID teacherId;
    private Integer semester;
    private List<Long> requirements;
}
