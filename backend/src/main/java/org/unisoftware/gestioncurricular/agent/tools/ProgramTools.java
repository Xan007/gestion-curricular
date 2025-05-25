package org.unisoftware.gestioncurricular.agentTools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.unisoftware.gestioncurricular.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.entity.StudyPlanEntry;
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

    @Tool(name = "listarProgramas", description = "Lista todos los programas académicos disponibles")
    public String listAllPrograms() {
        List<ProgramDTO> programs = programService.getAllPrograms();
        if (programs.isEmpty()) {
            return "No hay programas registrados.";
        }

        return programs.stream()
                .map(p -> "- " + p.getName())
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "obtenerPlanEstudios", description = "Devuelve el plan de estudios de un programa académico por su nombre")
    public String getStudyPlanByProgramName(
            @ToolParam(description = "Nombre del programa") String name,
            @ToolParam(description = "Año del plan (opcional)", required = false) Integer year
    ) {
        return "No me es posible mostrarte el plan de estudios, sin embargo tu puedes consultar el plan de estudios en este mismo software.";
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
