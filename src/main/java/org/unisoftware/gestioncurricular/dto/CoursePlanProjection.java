package org.unisoftware.gestioncurricular.dto;

import java.util.List;

public interface CoursePlanProjection {
    Long getId();
    String getName();
    String getType();
    Integer getCredits();
    String getRelation();
    String getArea();
    String getCycle();
    Integer getSemester();
    List<Long> getRequirements();
}
