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
    private Long id; // snies
    private String name; // 'nombre' translated to 'name'
    private String professionalProfile; // 'perfilProfesional' translated to 'professionalProfile'
    private String occupationalProfile; // 'perfilOcupacional' translated to 'occupationalProfile'
    private String admissionProfile; // 'perfilIngreso' translated to 'admissionProfile'
    private String competencies; // 'competencias' stays as 'competencies'
    private Long learningOutcomesFileId; // 'resultadosAprendizajeFileId' translated to 'learningOutcomesFileId'
    private Integer duration; // 'duracion' translated to 'duration'
    private String awardingDegree; // 'tituloOtorga' translated to 'awardingDegree'
}
