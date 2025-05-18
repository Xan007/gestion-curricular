package org.unisoftware.gestioncurricular.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.unisoftware.gestioncurricular.service.CourseService;
import org.unisoftware.gestioncurricular.dto.CourseDTO;

import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.unisoftware.gestioncurricular.util.courseParser.CourseFileParser;

@RestController
@RequestMapping("/cursos")
@Tag(name = "Cursos", description = "Operaciones relacionadas con los cursos")
public class CourseController {

    private final CourseService courseService;
    private final List<CourseFileParser> parsers;

    @Autowired
    public CourseController(CourseService courseService,
                            List<CourseFileParser> parsers) {
        this.courseService = courseService;
        this.parsers = parsers;
    }

    @PreAuthorize("hasRole('DECANO')")
    @PostMapping("/upload")
    @Operation(
            summary = "Subir cursos",
            description = "Permite subir un archivo con información de los cursos. **Requiere un token de autorización y el rol 'DECANO'.**",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Archivo con los datos de los cursos",
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
                            description = "Cursos subidos exitosamente."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Prohibido. El usuario no tiene los permisos necesarios para acceder a este recurso."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Error en el archivo enviado o tipo de archivo no soportado."
                    )
            }
    )
    public ResponseEntity<String> uploadCourses(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No se ha seleccionado ningún archivo.");
        }

        String filename = file.getOriginalFilename().toLowerCase();
        CourseFileParser parser = parsers.stream()
                .filter(p -> p.supports(filename))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported file type: " + filename));

        try (InputStream is = file.getInputStream()) {
            List<CourseDTO> dtos = parser.parse(is);
            courseService.processCourses(dtos);
            return ResponseEntity.ok("Courses uploaded successfully");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('DECANO')")
    @PutMapping("/{courseId}")
    @Operation(
            summary = "Actualizar curso",
            description = "Actualiza la información de un curso existente. **Requiere rol DECANO.**"
    )
    public ResponseEntity<CourseDTO> updateCourse(
            @Parameter(description = "ID del curso a actualizar", required = true)
            @PathVariable Long courseId,
            @RequestBody CourseDTO courseDTO) {

        CourseDTO updatedCourse = courseService.updateCourse(courseId, courseDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Obtener curso", description = "Obtiene la información de un curso por su ID.")
    public ResponseEntity<CourseDTO> getCourse(
            @Parameter(description = "ID del curso", required = true)
            @PathVariable Long courseId) {
        CourseDTO dto = courseService.getCourse(courseId);

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(
            summary = "Listar cursos",
            description = "Obtiene una lista de todos los cursos o filtra por programa si se provee el parámetro 'programId'."
    )
    public ResponseEntity<List<CourseDTO>> listCourses(
            @Parameter(description = "ID del programa para filtrar (opcional)")
            @RequestParam(value = "programId", required = false) Long programId
    ) {
        List<CourseDTO> courses;
        if (programId == null) {
            courses = courseService.getAllCourses();
        } else {
            courses = courseService.getCoursesByProgramId(programId);
        }
        return ResponseEntity.ok(courses);
    }


    @GetMapping("/buscar")
    @Operation(summary = "Buscar curso por nombre", description = "Busca un curso por su nombre.")
    public ResponseEntity<CourseDTO> searchByName(
            @Parameter(description = "Nombre del curso", required = true)
            @RequestParam("nombre") String nombre
    ) {
        CourseDTO dto = courseService.getCourseByName(nombre);
        return ResponseEntity.ok(dto);
    }
}
