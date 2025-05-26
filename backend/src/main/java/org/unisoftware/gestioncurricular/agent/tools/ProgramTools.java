package org.unisoftware.gestioncurricular.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.unisoftware.gestioncurricular.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.service.ProgramService;

import java.util.List;
import java.util.stream.Collectors;

public class ProgramTools {

    private final ProgramService programService;

    @Autowired
    public ProgramTools(ProgramService programService) {
        this.programService = programService;
    }

    @Tool(name = "buscarProgramaPorNombre", description = "Busca un programa académico por su nombre")
    public String searchProgramByName(@ToolParam(description = "Nombre del programa a buscar") String name) {
        ProgramDTO dto = programService.findProgramByName(name);
        if (dto == null) {
            return "No se encontró ningún programa con el nombre: " + name;
        }

        return String.format(
                "Programa: %s\nDuración: %d semestres\nNivel de formación: %s\nModalidad: %s\nTítulo que otorga: %s",
                dto.getName(),
                dto.getDuration(),
                dto.getAcademicLevel(),
                dto.getModality(),
                dto.getAwardingDegree()
        );
    }

    @Tool(name = "listarProgramas", description = "Lista todos los programas académicos disponibles. Usa esta función para ver todos los programas sin importar el nombre")
    public String listAllPrograms() {
        List<ProgramDTO> programs = programService.getAllPrograms();
        if (programs.isEmpty()) {
            return "No hay programas registrados.";
        }

        return programs.stream()
                .map(p -> "- " + p.getName())
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "obtenerPlanEstudios", description = "Devuelve el plan de estudios de un programa académico por su nombre y opcionalmente por el año, sino se utiliza el ultimo. Esta funcion puede ser utilizada para saber el prerequisito de ciertos cursos teniendo en cuenta el id y buscandolo")
    public String getStudyPlanByProgramName(
            @ToolParam(description = "Nombre del programa") String name,
            @ToolParam(description = "Año del plan (opcional)", required = false) Long year
    ) {
        ProgramDTO program = programService.findProgramByName(name);
        if (program == null) {
            return "Programa no encontrado: " + name;
        }

        Long programId = program.getId();

        if (year == null) {
            List<Integer> years = programService.getStudyPlanYears(programId);
            if (years.isEmpty()) {
                return "No hay planes de estudio registrados para el programa: " + name;
            }
            year = Long.valueOf(years.get(0));
        }

        List<String> courses = programService.getStudyPlan(programId, year)
                .stream()
                .map(course -> String.format("%s (Créditos: %d, Semestre: %d)",
                        course.getName(),
                        course.getCredits(),
                        course.getSemester() != null ? course.getSemester() : 0))
                .toList();

        if (courses.isEmpty()) {
            return String.format("No hay cursos registrados para el plan de estudios del año %d en el programa %s.", year, name);
        }

        return String.format("Plan de estudios para %s, año %d:\n%s",
                name,
                year,
                String.join("\n", courses));
    }

    @Tool(name = "listarAniosPlanEstudios", description = "Lista los años disponibles de plan de estudios para un programa")
    public String listStudyPlanYears(
            @ToolParam(description = "Nombre del programa") String name
    ) {
        ProgramDTO dto = programService.findProgramByName(name);
        if (dto == null) {
            return "Programa no encontrado: " + name;
        }

        List<Integer> years = programService.getStudyPlanYears(dto.getId());
        if (years.isEmpty()) {
            return "No hay años registrados para el plan de estudios de: " + name;
        }

        return "Años disponibles para el plan de estudios:\n" +
                years.stream().map(String::valueOf).collect(Collectors.joining("\n"));
    }
}
