package org.unisoftware.gestioncurricular.entity.files;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.unisoftware.gestioncurricular.entity.Program;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "programa_resultados_files")
@Getter
@Setter
public class ProgramaResultadosFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "object_id", nullable = false)
    private UUID fileId;

    @Column(name = "is_main", nullable = false)
    private boolean isMain;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;
}
