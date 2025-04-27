package org.unisoftware.gestioncurricular.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
/**
 * Data Transfer Object for the Program entity (programas).
 */
@Getter
@Setter
@AllArgsConstructor
public class ProgramDTO {
    private Long id;
    private String name;
    private String professionalProfile;
    private String occupationalProfile;
    private String admissionProfile;
    private String competencies;
    private Long learningOutcomesFileId;
    private Integer duration;
    private String awardingDegree;
}
