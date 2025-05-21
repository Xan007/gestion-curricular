package org.unisoftware.gestioncurricular.controller.files;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.dto.ProgramFileDTO;
import org.unisoftware.gestioncurricular.dto.UpdateProgramFileDTO;
import org.unisoftware.gestioncurricular.service.files.ProgramFileService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/programas/{programId}/files")
@RequiredArgsConstructor
@Tag(name = "Program Files", description = "Gestión de currículums y resultados de aprendizaje de programas")
public class ProgramFileController {

    private final ProgramFileService programFileService;

    @Operation(
            summary = "Generar URL para subir curriculum de un programa",
            description = "Requiere rol DECANO"
    )
    @PreAuthorize("hasRole('DECANO')")
    @GetMapping("/curriculums/upload-url")
    public ResponseEntity<String> generateCurriculumUploadUrl(
            @Parameter(description = "ID del programa") @PathVariable Long programId,
            @Parameter(description = "Fecha para nombrar el archivo. Puede ser solo año o año-mes-día (yyyy-MM-dd). Ejemplo: 2024 o 2024-03-02")
            @RequestParam(required = false) String dateStr
    ) {
        LocalDate date = null;
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                try {
                    date = LocalDate.parse(dateStr + "-01-01");
                } catch (Exception ex) {
                    return ResponseEntity.badRequest().body("Fecha inválida, debe ser yyyy o yyyy-MM-dd");
                }
            }
        }

        String url = programFileService.generateCurriculumUploadUrl(programId, date);
        return ResponseEntity.ok(url);
    }

    @Operation(
            summary = "Obtener URL del curriculum principal"
    )
    @GetMapping("/curriculums/main")
    public ResponseEntity<String> getMainCurriculumUrl(@PathVariable Long programId) {
        Optional<String> urlOpt = programFileService.getMainCurriculumUrl(programId);
        return urlOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar todos los curriculums de un programa"
    )
    @GetMapping("/curriculums")
    public ResponseEntity<List<ProgramFileDTO>> listCurriculums(
            @PathVariable Long programId,
            @RequestParam(defaultValue = "date") String orderBy,
            @RequestParam(defaultValue = "desc") String orderDir
    ) {
        List<ProgramFileDTO> urls = programFileService.listCurriculums(programId, orderBy, orderDir);
        return ResponseEntity.ok(urls);
    }

    // --- RESULTADOS DE APRENDIZAJE ---

    @Operation(
            summary = "Generar URL para subir resultados de aprendizaje de un programa",
            description = "Requiere rol DECANO"
    )
    @PreAuthorize("hasRole('DECANO')")
    @GetMapping("/results/upload-url")
    public ResponseEntity<String> generateResultsUploadUrl(
            @PathVariable Long programId,
            @RequestParam(required = false) String dateStr
    ) {
        LocalDate date = null;
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                try {
                    date = LocalDate.parse(dateStr + "-01-01");
                } catch (Exception ex) {
                    return ResponseEntity.badRequest().body("Fecha inválida, debe ser yyyy o yyyy-MM-dd");
                }
            }
        }
        String url = programFileService.generateResultadosUploadUrl(programId, date);
        return ResponseEntity.ok(url);
    }

    @Operation(
            summary = "Obtener URL del resultado de aprendizaje principal"
    )
    @GetMapping("/results/main")
    public ResponseEntity<String> getMainResultUrl(@PathVariable Long programId) {
        Optional<String> urlOpt = programFileService.getMainResultadoUrl(programId);
        return urlOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar todos los resultados de aprendizaje de un programa"
    )
    @GetMapping("/results")
    public ResponseEntity<List<ProgramFileDTO>> listResults(
            @PathVariable Long programId,
            @RequestParam(defaultValue = "date") String orderBy,
            @RequestParam(defaultValue = "desc") String orderDir
    ) {
        List<ProgramFileDTO> urls = programFileService.listResultados(programId, orderBy, orderDir);
        return ResponseEntity.ok(urls);
    }

    @Operation(
            summary = "Editar atributos de un curriculum (fecha, isMain)",
            description = "Requiere rol DECANO"
    )
    @PreAuthorize("hasRole('DECANO')")
    @PutMapping("/curriculums/{curriculumId}")
    public ResponseEntity<Void> editCurriculumFile(
            @PathVariable Long programId,
            @PathVariable Long curriculumId,
            @RequestBody UpdateProgramFileDTO dto
    ) {
        programFileService.editCurriculumFile(programId, curriculumId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Editar atributos de un resultado de aprendizaje (fecha, isMain)",
            description = "Requiere rol DECANO"
    )
    @PreAuthorize("hasRole('DECANO')")
    @PutMapping("/resultados/{resultadosId}")
    public ResponseEntity<Void> updateResultadosFile(
            @PathVariable Long programId,
            @PathVariable Long resultadosId,
            @RequestBody UpdateProgramFileDTO dto
    ) {
        programFileService.editResultadosFile(programId, resultadosId, dto);
        return ResponseEntity.noContent().build();
    }

}
