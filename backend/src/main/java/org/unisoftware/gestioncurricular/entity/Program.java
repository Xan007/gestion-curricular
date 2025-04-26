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
    private Long id;

    @Column(name = "nombre")
    private String name;

    @Column(name = "perfil_profesional")
    private String perfilProfesional;

    @Column(name = "perfil_ocupacional")
    private String perfilOcupacional;

    @Column(name = "perfil_ingreso")
    private String perfilIngreso;

    @Column(name = "competencias")
    private String competencias;

    @Column(name = "resultados_aprendizaje_file_id")
    private Long resultadosAprendizajeFileId;

    @Column(name = "duracion")
    private Integer duration;

    @Column(name = "titulo_otorga")
    private String awardingDegree;

    // Getters and Setters
}
