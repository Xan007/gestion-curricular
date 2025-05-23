package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.util.enums.programEnums.ProgramAcademicLevelType;
import org.unisoftware.gestioncurricular.util.enums.programEnums.ProgramModalityType;

@Entity
@Table(name = "programas")
@Getter
@Setter
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String name;

    @Column(name = "duracion")
    private Integer duration;

    @Column(name = "nivel_formacion")
    @Enumerated(EnumType.STRING)
    private ProgramAcademicLevelType academicLevel;

    @Column(name = "modalidad")
    @Enumerated(EnumType.STRING)
    private ProgramModalityType modality;




    @Column(name = "titulo_otorga")
    private String awardingDegree;

    // Getters and Setters
}
