package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "perfil_profesional")
    private String professionalProfile;

    @Column(name = "perfil_ocupacional")
    private String occupationalProfile;

    @Column(name = "perfil_ingreso")
    private String admissionProfile;

    @Column(name = "competencias")
    private String competencies;

    @Column(name = "duracion")
    private Integer duration;


    @Column(name = "titulo_otorga")
    private String awardingDegree;

    // Getters and Setters
}
