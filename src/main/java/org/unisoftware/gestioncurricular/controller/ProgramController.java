package org.unisoftware.gestioncurricular.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.unisoftware.gestioncurricular.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.service.ProgramService;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/programas")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping("/{programId}/plan-estudio/upload")
    public ResponseEntity<String> uploadPlan(
            @PathVariable Long programId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // Validación de archivo vacío o nulo
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha enviado ningún archivo.");
            }

            // Llamada al servicio para manejar la carga
            programService.uploadStudyPlan(programId, file);

            // Respuesta exitosa
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Plan de estudios cargado exitosamente.");

        } catch (Exception e) {
            // En caso de un error, devolver un mensaje adecuado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hubo un error al cargar el plan de estudios: " + e.getMessage());
        }
    }

    @GetMapping("/{programId}")
    public ResponseEntity<ProgramDTO> getProgram(@PathVariable Long programId) {
        ProgramDTO dto = programService.getProgram(programId);
        return ResponseEntity.ok(dto);
    }

    /** Crea un nuevo Programa y devuelve su ID */
    @PostMapping("/crear")
    public ResponseEntity<Long> createProgram(@RequestBody ProgramDTO dto) {
        Long id = programService.createProgram(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

}
