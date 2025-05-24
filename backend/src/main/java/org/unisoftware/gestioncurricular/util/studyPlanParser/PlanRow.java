package org.unisoftware.gestioncurricular.util.studyPlanParser;

import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseArea;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseCycle;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseType;

import java.util.List;

@Setter
@Getter
public class PlanRow {
    // Getters and Setters
    private Long snies;
    private String courseName;
    private CourseType courseType;
    private CourseCycle courseCycle;
    private CourseArea courseArea;
    private Integer credits;
    private String relation;
    private Integer semester;
    private List<Long> requisitos;

    public PlanRow() {}

    @Override
    public String toString() {
        return "PlanRow{" +
                "snies=" + snies +
                ", courseName='" + courseName + '\'' +
                ", courseType=" + courseType +
                ", courseCycle=" + courseCycle +
                ", courseArea=" + courseArea +
                ", credits=" + credits +
                ", relation='" + relation + '\'' +
                ", semester=" + semester +
                ", requisitos=" + requisitos +
                '}';
    }
}