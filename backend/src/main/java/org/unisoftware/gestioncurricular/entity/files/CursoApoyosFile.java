package org.unisoftware.gestioncurricular.entity.files;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.unisoftware.gestioncurricular.entity.Course;
import org.unisoftware.gestioncurricular.entity.converters.AcademicSupportTypeConverter;
import org.unisoftware.gestioncurricular.util.enums.AcademicSupportType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "curso_apoyos_files")
@Getter
@Setter
public class CursoApoyosFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "object_id", nullable = false)
    private UUID fileId;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Convert(converter = AcademicSupportTypeConverter.class)
    @ColumnTransformer(
            read  = "CAST(tipo AS VARCHAR)",
            write = "?::tipo_apoyo_academico"
    )
    @Column(name = "tipo", nullable = false, columnDefinition = "tipo_apoyo_academico")
    private AcademicSupportType tipo;


}

