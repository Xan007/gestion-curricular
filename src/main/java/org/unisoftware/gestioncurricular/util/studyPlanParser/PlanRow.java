package org.unisoftware.gestioncurricular.util.studyPlanParser;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlanRow {
    private Long snies;
    private Integer semester;
    private List<Long> requisitos;
}
