package org.unisoftware.gestioncurricular.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.nio.file.Path;
import java.time.Year;
import java.util.List;

@RestController
@RequestMapping("/programas")
@Tag(name = "Programas", description = "Operaciones relacionadas con los programas académicos")
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
    @PostMapping("/{programId}/upload-plan")
    @Operation(
            summary = "Subir plan de estudios",
            description = "Permite subir un archivo con el plan de estudios completo de un programa académico. " +
                    "El archivo debe contener las columnas: SNIES, Curso, Tipo, Ciclo, Área, Créditos, Relación, Semestre, Requisito(s). " +
                    "**Requiere un token de autorización y el rol 'DIRECTOR_DE_PROGRAMA'.**",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Archivo con el plan de estudios del programa (CSV o Excel)",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "multipart/form-data",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    type = "string",
                                    format = "binary"
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Plan de estudios cargado exitosamente."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Error en los datos del archivo o formato incorrecto."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Prohibido. El usuario no tiene los permisos necesarios para acceder a este recurso."
                    )
            }
    )
    public ResponseEntity<String> uploadPlan(
            @Parameter(description = "ID del programa", required = true)
            @PathVariable Long programId,
            @Parameter(description = "Año del plan de estudios (opcional, por defecto el año actual)")
            @RequestParam(value = "year", required = false) Long year,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No se ha enviado ningún archivo.");
            }

            String filename = file.getOriginalFilename();
            if (filename == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El archivo no tiene un nombre válido.");
            }

            StudyPlanParser parser = studyPlanParsers.stream()
                    .filter(p -> p.supports(filename.toLowerCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Tipo de archivo no soportado: " + filename +
                                    ". Formatos soportados: CSV (.csv), Excel (.xlsx, .xls)"));

            List<PlanRow> planRows = parser.parse(file);

            if (planRows.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El archivo está vacío o no contiene datos válidos.");
            }

            int parsedYear = (year != null) ? year.intValue() : Year.now().getValue();
            programService.processStudyPlan(programId, planRows, parsedYear);

            return ResponseEntity.ok("Plan de estudios cargado exitosamente para el año " + parsedYear + ".");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al procesar el archivo: " + e.getMessage());
        }
    }

    @GetMapping("/{programId}")
    @Operation(summary = "Obtener programa", description = "Obtiene la información de un programa por su ID.")
    public ResponseEntity<ProgramDTO> getProgram(
            @Parameter(description = "ID del programa", required = true)
            @PathVariable Long programId) {
        ProgramDTO dto = programService.getProgram(programId);
        dto.setId(programId);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('DECANO')")
    @PostMapping("/crear")
    @Operation(
            summary = "Crear programa",
            description = "Crea un nuevo programa académico. **Requiere un token de autorización y el rol 'DECANO'.**",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "201",
                            description = "Programa creado exitosamente."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Prohibido. El usuario no tiene los permisos necesarios para acceder a este recurso."
                    )
            }
    )
    public ResponseEntity<Long> createProgram(@RequestBody ProgramDTO dto) {
        Long id = programService.createProgram(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{programId}/plan-estudio")
    @Operation(summary = "Obtener plan de estudios", description = "Obtiene el plan de estudios de un programa por su ID y año. Si no se especifica el año, se devuelve el último plan registrado.")
    public ResponseEntity<List<StudyPlanEntry>> getStudyPlan(
            @Parameter(description = "ID del programa", required = true)
            @PathVariable Long programId,

            @Parameter(description = "Año del plan de estudios (opcional). Si no se proporciona, se usará el más reciente.")
            @RequestParam(required = false) Integer year
    ) {
        List<StudyPlanEntry> plan = programService.getStudyPlan(programId, year != null ? year.longValue() : null);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/{programId}/plan-estudio/years")
    @Operation(
            summary = "Obtener años disponibles de planes de estudio",
            description = "Obtiene todos los años para los cuales existen planes de estudio registrados para un programa específico.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Lista de años disponibles obtenida exitosamente.",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                                            type = "array",
                                            example = "[2023, 2024, 2025]"
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Programa no encontrado."
                    )
            }
    )
    public ResponseEntity<List<Integer>> getStudyPlanYears(
            @Parameter(description = "ID del programa", required = true)
            @PathVariable Long programId) {
        try {
            List<Integer> years = programService.getStudyPlanYears(programId);
            return ResponseEntity.ok(years);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar programa por nombre", description = "Busca un programa por su nombre.")
    public ResponseEntity<ProgramDTO> findProgramByName(
            @Parameter(description = "Nombre del programa", required = true)
            @RequestParam("nombre") String name) {
        ProgramDTO dto = programService.findProgramByName(name);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(summary = "Listar todos los programas", description = "Obtiene una lista de todos los programas académicos.")
    public ResponseEntity<List<ProgramDTO>> getAllPrograms() {
        List<ProgramDTO> programs = programService.getAllPrograms();
        return ResponseEntity.ok(programs);
    }

}
