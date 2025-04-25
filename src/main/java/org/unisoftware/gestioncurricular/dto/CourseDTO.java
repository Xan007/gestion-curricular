package org.unisoftware.gestioncurricular.dto;

import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseArea;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseCycle;
import org.unisoftware.gestioncurricular.util.courseEnums.CourseType;

@Getter
@Setter
public class CourseDTO {
    private Long id;
    private String name;
    private CourseType type;  // Usamos el Enum
    private CourseCycle cycle;      // Usamos el Enum
    private CourseArea area;        // Usamos el Enum
    private Integer credits;
    private String relation;

    public CourseDTO(Long id, String name, String type, String cycle, String area, Integer credits, String relation) {
        this.id = id;
        this.name = name;
        this.type = CourseType.fromCode(type);
        this.cycle = CourseCycle.fromCode(cycle);
        this.area = CourseArea.fromCode(area);

        this.credits = credits;
        this.relation = relation != null && !relation.isEmpty() ? relation : "1:1";
    }
}
