package org.unisoftware.gestioncurricular.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.util.enums.programEnums.ProgramAcademicLevelType;
import org.unisoftware.gestioncurricular.util.enums.programEnums.ProgramModalityType;

@Getter
@Setter
@AllArgsConstructor
public class ProgramDTO {
    private Long id;
    private String name;
    private Integer duration;
    private String awardingDegree;
    private ProgramAcademicLevelType academicLevel;
    private ProgramModalityType modality;
}
