package org.unisoftware.gestioncurricular.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.unisoftware.gestioncurricular.entity.converters.CourseAreaConverter;
import org.unisoftware.gestioncurricular.entity.converters.CourseCycleConverter;
import org.unisoftware.gestioncurricular.entity.converters.CourseTypeConverter;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseArea;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseCycle;
import org.unisoftware.gestioncurricular.util.enums.courseEnums.CourseType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cursos")
@Setter
@Getter
public class Course {

    @Id
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "name")
    private String name;

    @Column(name = "tipo", columnDefinition = "curso_types")
    @ColumnTransformer(read = "CAST(tipo AS VARCHAR)", write = "?::curso_types")
    @Convert(converter = CourseTypeConverter.class)
    private CourseType type;

    @Column(name = "creditos")
    private Integer credits;

    @Column(name = "relacion")
    private String relation;

    @Column(name = "area", columnDefinition = "curso_areas")
    @ColumnTransformer(read = "CAST(area AS VARCHAR)", write = "?::curso_areas")
    @Convert(converter = CourseAreaConverter.class)
    private CourseArea area;

    @Column(name = "ciclo", columnDefinition = "curso_ciclo")
    @ColumnTransformer(read = "CAST(ciclo AS VARCHAR)", write = "?::curso_ciclo")
    @Convert(converter = CourseCycleConverter.class)
    private CourseCycle cycle;

    @Column(name = "docente_id")
    private UUID teacherId;
}
