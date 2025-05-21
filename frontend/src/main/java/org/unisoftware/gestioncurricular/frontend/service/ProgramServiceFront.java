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
     * Obtiene el plan de estudios de un programa por su ID
     */
    public List<StudyPlanEntryDTO> getStudyPlan(Long programaId) throws Exception {
        URL url = new URL(BASE_URL + "/" + programaId + "/plan-estudio");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<List<StudyPlanEntryDTO>>() {});
        }
    }}
