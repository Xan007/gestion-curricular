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
    private Integer duration;
    private String awardingDegree;
}
