package org.unisoftware.gestioncurricular.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseArea;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseCycle;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseType;

import java.util.List;

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
    private String teacherId;
    private Integer semester;
    private List<Long> requirements;

    /* public CourseDTO(Long id, String name, String type, String cycle, String area, Integer credits, String relation) {
        this.id = id;
        this.name = name;
        this.type = CourseType.fromCode(type);
        this.cycle = CourseCycle.fromCode(cycle);
        this.area = CourseArea.fromCode(area);

        this.credits = credits;
        this.relation = relation != null && !relation.isEmpty() ? relation : "1:1";
    }*/
}
