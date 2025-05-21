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

    private static final String BASE_URL = "http://localhost:8080/cursos";

    private String getToken() {
        return org.unisoftware.gestioncurricular.frontend.util.SessionManager.getInstance().getToken();
    }

    public List<CourseDTO> listCourses() throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + getToken());
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<List<CourseDTO>>() {});
        }
    }

    public List<CourseDTO> listCoursesByProgramaId(Long programaId) throws Exception {
        String urlStr = BASE_URL + "?programId=" + programaId;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + getToken());
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<List<CourseDTO>>() {});
        }
    }

    public List<CourseDTO> listCoursesByDocenteId(String docenteId) throws Exception {
        String urlStr = BASE_URL + "/docente/" + docenteId;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + getToken());
        try (InputStream in = conn.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<List<CourseDTO>>() {});
        }
    }

    public CourseDTO assignTeacher(Long courseId, String docenteId) throws Exception {
        String urlStr = BASE_URL + "/" + courseId + "/asignar-docente?docenteId=" + docenteId;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + getToken());
        conn.setDoOutput(true);
        int responseCode = conn.getResponseCode();
        ObjectMapper mapper = new ObjectMapper();
        if (responseCode == 200) {
            try (InputStream in = conn.getInputStream()) {
                return mapper.readValue(in, CourseDTO.class);
            }
        } else {
            String errorMsg = "";
            try (InputStream err = conn.getErrorStream()) {
                if (err != null) {
                    errorMsg = new String(err.readAllBytes());
                }
            }
            throw new RuntimeException("Error al asignar docente: " + conn.getResponseMessage() + (errorMsg.isEmpty() ? "" : ". Detalle: " + errorMsg));
        }
    }

    public CourseDTO updateCourse(CourseDTO course) throws Exception {
        String urlStr = BASE_URL + "/" + course.getId();
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + getToken());
        conn.setDoOutput(true);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String json = mapper.writeValueAsString(course);
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (InputStream in = conn.getInputStream()) {
                return mapper.readValue(in, CourseDTO.class);
            }
        } else {
            String errorMsg = "";
            try (InputStream err = conn.getErrorStream()) {
                if (err != null) {
                    errorMsg = new String(err.readAllBytes());
                }
            }
            throw new RuntimeException("Error al actualizar curso: " + conn.getResponseMessage() + (errorMsg.isEmpty() ? "" : ". Detalle: " + errorMsg));
        }
    }
}
