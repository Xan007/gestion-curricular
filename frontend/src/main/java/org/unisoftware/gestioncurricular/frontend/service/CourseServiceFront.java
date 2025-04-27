package org.unisoftware.gestioncurricular.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.unisoftware.gestioncurricular.frontend.dto.CourseDTO;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.io.InputStream;

@Service
public class CourseServiceFront {

    // Cambia la URL base según tu configuración real
    private static final String BASE_URL = "http://localhost:8080/cursos";

    public List<CourseDTO> listCourses() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<List<CourseDTO>>() {});
        }
    }
}