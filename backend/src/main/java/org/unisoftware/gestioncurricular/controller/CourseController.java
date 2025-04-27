package org.unisoftware.gestioncurricular.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.unisoftware.gestioncurricular.config.customAnnotations.Public;
import org.unisoftware.gestioncurricular.service.CourseService;
import org.unisoftware.gestioncurricular.dto.CourseDTO;

import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.unisoftware.gestioncurricular.util.courseParser.CourseFileParser;

@RestController
@RequestMapping("/cursos")
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
    public ResponseEntity<String> uploadCourses(@RequestParam("file") MultipartFile file) {
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

    @Public
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourse(
            @PathVariable Long courseId
    ) {
        CourseDTO dto = courseService.getCourse(courseId);

        return ResponseEntity.ok(dto);
    }

    @Public
    @GetMapping
    public ResponseEntity<List<CourseDTO>> listAll() {
        return ResponseEntity.ok(
                courseService.getAllCourses()
        );
    }

    @Public
    @GetMapping("/buscar")
    public ResponseEntity<CourseDTO> searchByName(
            @RequestParam("nombre") String nombre
    ) {
        CourseDTO dto = courseService.getCourseByName(nombre);
        return ResponseEntity.ok(dto);
    }
}
