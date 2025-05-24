package org.unisoftware.gestioncurricular.controller.files;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.dto.files.CourseMicroFileDTO;
import org.unisoftware.gestioncurricular.dto.files.CourseSupportFileDTO;
import org.unisoftware.gestioncurricular.dto.files.UpdateCourseMicroFileDTO;
import org.unisoftware.gestioncurricular.dto.files.UpdateCourseSupportFileDTO;
import org.unisoftware.gestioncurricular.service.files.CourseFileService;
import org.unisoftware.gestioncurricular.util.enums.AcademicSupportType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/courses/{courseId}/files")
@RequiredArgsConstructor
@Tag(name = "Course Files", description = "Gestión de archivos de apoyos académicos y microcurrículos para cursos")
public class CourseFileController {
    private final CourseFileService courseFileService;

    @Operation(
            summary = "Generar URL para subir archivo de apoyo académico",
            description = "Genera una URL prefirmada para que un docente suba un archivo de apoyo académico a un curso. **Requiere rol DOCENTE**",
            parameters = {
                    @Parameter(name = "courseId", description = "ID del curso", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "filename", description = "Nombre del archivo con extensión", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "URL generada exitosamente"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @PreAuthorize("hasRole('DIRECTOR_DE_PROGRAMA')")
    @GetMapping("/apoyos/upload-url")
    public ResponseEntity<String> generateApoyoUploadUrl(
            @PathVariable Long courseId,
            @RequestParam @NotBlank String filename
    ) {
        String url = courseFileService.generateApoyoUploadUrl(courseId, filename);
        return ResponseEntity.ok(url);
    }

    @Operation(
            summary = "Listar archivos de apoyo académico",
            description = "Obtiene una lista de URLs de archivos de apoyo académico asociados a un curso. **Sin restricción de rol**",
            parameters = {
                    @Parameter(name = "courseId", description = "ID del curso", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "tipo", description = "Filtrar por tipo de apoyo académico", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de URLs obtenida correctamente")
            }
    )
    @GetMapping("/apoyos")
    public ResponseEntity<List<CourseSupportFileDTO>> getAllApoyos(
            @PathVariable Long courseId,
            @RequestParam(required = false) AcademicSupportType tipo
    ) {
        List<CourseSupportFileDTO> urls = courseFileService.getAllApoyos(courseId, Optional.ofNullable(tipo));
        return ResponseEntity.ok(urls);
    }

    @Operation(
            summary = "Generar URL para subir microcurrículo",
            description = "Genera una URL prefirmada para que el director de programa suba un microcurrículo. **Requiere rol DIRECTOR_DE_PROGRAMA**",
            parameters = {
                    @Parameter(name = "courseId", description = "ID del curso", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "date", description = "Fecha a asociar con el archivo", schema = @Schema(type = "string", format = "date"), required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "URL generada exitosamente"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
            }
    )
    @PreAuthorize("hasRole('DIRECTOR_DE_PROGRAMA')")
    @GetMapping("/microcurriculos/upload-url")
    public ResponseEntity<String> generateMicrocurriculumUploadUrl(
            @PathVariable Long courseId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        if (date == null) {
            date = LocalDate.now();
        }
        String url = courseFileService.generateMicrocurriculumUploadUrl(courseId, date);
        return ResponseEntity.ok(url);
    }

    @Operation(
            summary = "Actualizar detalles de un microcurrículo",
            description = "Permite marcar un microcurrículo como principal, y actualizar la fecha de subida o la fecha asociada. **Requiere rol DIRECTOR_DE_PROGRAMA**",
            parameters = {
                    @Parameter(name = "courseId", description = "ID del curso", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "fileId", description = "ID del archivo de microcurrículo", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Detalles actualizados correctamente"),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado"),
                    @ApiResponse(responseCode = "404", description = "Archivo no encontrado")
            }
    )
    @PreAuthorize("hasRole('DIRECTOR_DE_PROGRAMA')")
    @PutMapping("/microcurriculos/{fileId}")
    public ResponseEntity<Void> updateMicroFileDetails(
            @PathVariable Long courseId,
            @PathVariable Long fileId,
            @RequestBody UpdateCourseMicroFileDTO dto
    ) {
        courseFileService.updateMicrocurriculumFileDetails(courseId, fileId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar microcurrículos de un curso",
            description = "Devuelve una lista de URLs de microcurrículos ordenados por fecha u otro criterio. **Sin restricción de rol**",
            parameters = {
                    @Parameter(name = "courseId", description = "ID del curso", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "sortBy", description = "Campo por el cual ordenar (ej. uploadedAt)", required = false),
                    @Parameter(name = "direction", description = "asc o desc", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de microcurrículos obtenida")
            }
    )
    @GetMapping("/microcurriculos")
    public ResponseEntity<List<CourseMicroFileDTO>> getAllMicrocurriculums(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        boolean ascending = direction.equalsIgnoreCase("asc");
        List<CourseMicroFileDTO> urls = courseFileService.getAllMicro(courseId, Optional.of(sortBy), ascending);
        return ResponseEntity.ok(urls);
    }

    @Operation(
            summary = "Actualizar archivo de apoyo académico",
            description = "Permite modificar un archivo de apoyo de un curso. Requiere rol DOCENTE."
    )
    @PreAuthorize("hasRole('DOCENTE')")
    @PutMapping("/apoyos/{fileId}")
    public ResponseEntity<Void> updateApoyoAcademico(
            @PathVariable Long courseId,
            @PathVariable UUID fileId,
            @RequestBody UpdateCourseSupportFileDTO dto
    ) {
        courseFileService.updateApoyoFileDetails(courseId, fileId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Obtener URL del microcurrículo principal",
            description = "Devuelve la URL del archivo marcado como principal para un curso. **Sin restricción de rol**",
            parameters = {
                    @Parameter(name = "courseId", description = "ID del curso", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "URL obtenida correctamente"),
                    @ApiResponse(responseCode = "404", description = "No se encontró un microcurrículo principal")
            }
    )
    @GetMapping("/microcurriculos/main")
    public ResponseEntity<String> getMainMicrocurriculum(
            @PathVariable Long courseId
    ) {
        String url = courseFileService.getMainMicrocurriculum(courseId);
        if (url == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(url);
    }
}
