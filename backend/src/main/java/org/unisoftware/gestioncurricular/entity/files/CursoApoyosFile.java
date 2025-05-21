package org.unisoftware.gestioncurricular.entity.files;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.entity.Course;

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
    @JoinColumn(name = "curso_id", nullable = false)
    private Course course;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}

