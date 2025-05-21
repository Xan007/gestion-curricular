package org.unisoftware.gestioncurricular.entity.files;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.entity.Course;

import java.time.LocalDateTime;

@Entity
@Table(name = "curso_microcurriculums_files")
@Getter
@Setter
public class CursoMicrocurriculumFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id", nullable = false)
    private Course course;

    @Column(name = "archivo_id", nullable = false)
    private String fileId;

    @Column(name = "is_main", nullable = false)
    private boolean isMain;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
