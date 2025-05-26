package org.unisoftware.gestioncurricular.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.unisoftware.gestioncurricular.dto.CourseDTO;
import org.unisoftware.gestioncurricular.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.service.CourseService;
import org.unisoftware.gestioncurricular.service.ProgramService;

import java.util.List;
import java.util.stream.Collectors;

public class CourseTools {

    private final CourseService courseService;
    private final ProgramService programService;

    @Autowired
    public CourseTools(CourseService courseService, ProgramService programService) {
        this.courseService = courseService;
        this.programService = programService;
    }

    @Tool(name = "buscarCursoPorNombre", description = "Busca un curso por su nombre")
    public String searchCourseByName(@ToolParam(description = "Nombre del curso a buscar") String name) {
        try {
            CourseDTO dto = courseService.getCourseByName(name);
            if (dto == null) {
                return "No se encontró ningún curso con el nombre: " + name;
            }
            return String.format(
                    "Curso: %s\nTipo: %s\nCréditos: %d\nÁrea: %s\nCiclo: %s\nRelación: %s",
                    dto.getName(),
                    dto.getType(),
                    dto.getCredits(),
                    dto.getArea(),
                    dto.getCycle(),
                    dto.getRelation() != null ? dto.getRelation() : "No aplica"
            );
        } catch (Exception e) {
            return "Error al buscar curso: " + e.getMessage();
        }
    }

    @Tool(name = "listarCursos", description = "Lista todos los cursos disponibles sin importar el programa")
    public String listAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        if (courses.isEmpty()) {
            return "No hay cursos registrados.";
        }
        return courses.stream()
                .map(c -> "- " + c.getName())
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "listarCursosPorPrograma", description = "Lista los cursos asociados a un programa académico por su nombre")
    public String listCoursesByProgramName(@ToolParam(description = "Nombre del programa") String programName) {
        ProgramDTO program = programService.findProgramByName(programName);
        if (program == null) {
            return "No se encontró ningún programa con el nombre: " + programName;
        }
        List<CourseDTO> courses = courseService.getCoursesByProgramId(program.getId());
        if (courses.isEmpty()) {
            return "No hay cursos asociados al programa: " + programName;
        }
        return "Cursos del programa " + programName + ":\n" +
                courses.stream()
                        .map(c -> "- " + c.getName())
                        .collect(Collectors.joining("\n"));
    }

    @Tool(name = "listarCursosPorProgramaYSemestre", description = "Lista los cursos de un programa específico en un año y semestre determinado")
    public String listCoursesByProgramAndSemester(
            @ToolParam(description = "Nombre del programa") String programName,
            @ToolParam(description = "Año del plan de estudios") Integer year,
            @ToolParam(description = "Número de semestre (ej: 1, 2, 3...)") Integer semester
    ) {
        ProgramDTO program = programService.findProgramByName(programName);
        if (program == null) {
            return "No se encontró ningún programa con el nombre: " + programName;
        }

        List<CourseDTO> courses = courseService.getCoursesByProgramAndSemester(program.getId(), year, semester);
        if (courses.isEmpty()) {
            return String.format("No hay cursos para el programa '%s' en el año %d y semestre %d.", programName, year, semester);
        }

        return String.format("Cursos del programa '%s' - Año %d, Semestre %d:\n", programName, year, semester) +
                courses.stream()
                        .map(c -> "- " + c.getName())
                        .collect(Collectors.joining("\n"));
    }
}
