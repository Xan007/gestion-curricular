package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

@Entity
@Immutable
@Table(name = "study_plan_mv")
@Getter
public class StudyPlanEntry {
    @EmbeddedId
    private StudyPlanEntryId id;

    @Column(name = "name")
    private String name;
    @Column(name = "type")
    private String type;
    @Column(name = "credits")
    private Integer credits;
    @Column(name = "relation")
    private String relation;
    @Column(name = "area")
    private String area;
    @Column(name = "cycle")
    private String cycle;
    @Column(name = "semester")
    private Integer semester;
    @Column(name = "requirements")
    private Long[] requirements;
}

