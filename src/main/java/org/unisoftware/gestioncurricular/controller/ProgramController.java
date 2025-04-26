package org.unisoftware.gestioncurricular.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.unisoftware.gestioncurricular.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.entity.StudyPlanEntry;
import org.unisoftware.gestioncurricular.service.ProgramService;
import org.unisoftware.gestioncurricular.util.studyPlanParser.StudyPlanParser;
import org.unisoftware.gestioncurricular.util.studyPlanParser.PlanRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/programas")
public class ProgramController {

    private final ProgramService programService;
    private final List<StudyPlanParser> studyPlanParsers;

    @Autowired
    public ProgramController(ProgramService programService,
                             List<StudyPlanParser> studyPlanParsers) {
        this.programService = programService;
        this.studyPlanParsers = studyPlanParsers;
    }

    @PreAuthorize("hasRole('DIRECTOR_DE_PROGRAMA')")
    @PostMapping("/{programId}/plan-estudio/upload")
    public ResponseEntity<String> uploadPlan(
            @PathVariable Long programId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha enviado ningún archivo.");
            }

            String filename = file.getOriginalFilename().toLowerCase();
            StudyPlanParser parser = studyPlanParsers.stream()
                    .filter(p -> p.supports(filename))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported file type: " + filename));

            List<PlanRow> planRows = parser.parse(file);
            programService.processStudyPlan(programId, planRows);

            return ResponseEntity.ok("Plan de estudios cargado exitosamente.");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al cargar el plan de estudios: " + e.getMessage());
        }
    }

    @GetMapping("/{programId}")
    public ResponseEntity<ProgramDTO> getProgram(@PathVariable Long programId) {
        ProgramDTO dto = programService.getProgram(programId);
        dto.setId(programId);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('DECANO')")
    @PostMapping("/crear")
    public ResponseEntity<Long> createProgram(@RequestBody ProgramDTO dto) {
        Long id = programService.createProgram(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{programId}/plan-estudio")
    public ResponseEntity<List<StudyPlanEntry>> getStudyPlan(@PathVariable Long programId) {
        List<StudyPlanEntry> plan = programService.getStudyPlan(programId);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/buscar")
    public ResponseEntity<ProgramDTO> findProgramByName(@RequestParam("nombre") String name) {
        ProgramDTO dto = programService.findProgramByName(name);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ProgramDTO>> getAllPrograms() {
        List<ProgramDTO> programs = programService.getAllPrograms();
        return ResponseEntity.ok(programs);
    }

}
