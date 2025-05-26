package org.unisoftware.gestioncurricular.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.unisoftware.gestioncurricular.frontend.dto.ProgramDTO;
import org.unisoftware.gestioncurricular.frontend.dto.StudyPlanEntryDTO;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.io.InputStream;

@Service
public class ProgramServiceFront {

    private static final String BASE_URL = "http://localhost:8080/programas";

    public List<ProgramDTO> listPrograms() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<List<ProgramDTO>>() {});
        }
    }

    /**
     * Obtiene el plan de estudios de un programa por su ID y año.
     * Si el año es null, se solicita el último plan registrado al backend.
     */
    public List<StudyPlanEntryDTO> getStudyPlan(Long programaId, Integer year) throws Exception {
        String urlString = BASE_URL + "/" + programaId + "/plan-estudio";
        if (year != null) {
            urlString += "?year=" + year;
        }
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<List<StudyPlanEntryDTO>>() {});
        }
    }

    /**
     * Obtiene el plan de estudios de un programa por su ID para un año específico.
     * @param programaId ID del programa
     * @param year Año del plan de estudios
     * @return Lista de entradas del plan de estudios para el año especificado
     */
    public List<StudyPlanEntryDTO> getStudyPlanByYear(Long programaId, Integer year) throws Exception {
        if (year == null) {
            throw new IllegalArgumentException("El año no puede ser nulo para este método");
        }
        String urlString = BASE_URL + "/" + programaId + "/plan-estudio?year=" + year;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<List<StudyPlanEntryDTO>>() {});
        }
    }

    /**
     * Obtiene el plan de estudios más reciente de un programa por su ID.
     * Este método es un wrapper para llamar a getStudyPlan(programaId, null).
     */
    public List<StudyPlanEntryDTO> getStudyPlan(Long programaId) throws Exception {
        return getStudyPlan(programaId, null);
    }

    /**
     * Obtiene los años disponibles del plan de estudios de un programa
     */
    public List<Integer> getAniosPlanEstudios(Long programaId) throws Exception {
        URL url = new URL(BASE_URL + "/" + programaId + "/plan-estudio/years");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<List<Integer>>() {});
        }
    }
}
