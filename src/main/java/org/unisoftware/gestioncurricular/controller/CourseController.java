package org.unisoftware.gestioncurricular.controller;

import org.springframework.http.ResponseEntity;
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
public class CourseController {

    private final CourseService courseService;
    private final List<CourseFileParser> parsers;

    @Autowired
    public CourseController(CourseService courseService,
                            List<CourseFileParser> parsers) {
        this.courseService = courseService;
        this.parsers = parsers;
    }

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
}
