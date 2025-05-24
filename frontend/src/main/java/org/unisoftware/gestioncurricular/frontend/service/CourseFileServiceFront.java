package org.unisoftware.gestioncurricular.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.unisoftware.gestioncurricular.frontend.dto.FileUploadInfoDTO;
// import org.unisoftware.gestioncurricular.frontend.util.SessionManager; // No se usa directamente aquí

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseFileServiceFront {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:8080";
    private static final Pattern UUID_PATTERN = Pattern.compile("([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})");

    private String extractFileIdFromUrlPath(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            Matcher matcher = UUID_PATTERN.matcher(path);
            String foundUuid = null;
            // Find the last UUID in the path, as it's likely the fileId
            while (matcher.find()) {
                foundUuid = matcher.group(1);
            }
            return foundUuid;
        } catch (Exception e) {
            System.err.println("Error extrayendo fileId de la URL: " + urlString + " - " + e.getMessage());
            return null;
        }
    }

    public FileUploadInfoDTO getApoyoUploadUrl(Long courseId, String filename, String token) throws IOException {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
        URL url = new URL(BASE_URL + "/courses/" + courseId + "/files/apoyos/upload-url?filename=" + encodedFilename);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String presignedUrlString = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("[CourseFileServiceFront] URL prefirmada para apoyo recibida del backend: " + presignedUrlString); // Diagnóstico
            String fileId = extractFileIdFromUrlPath(presignedUrlString); // Extraer fileId
            System.out.println("[CourseFileServiceFront] fileId extraído para apoyo: " + fileId); // Diagnóstico
            return new FileUploadInfoDTO(presignedUrlString, fileId); // Devolver fileId
        } else {
            String errorBody = "";
            if (conn.getErrorStream() != null) {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new IOException("Error al obtener URL de subida para apoyo: " + responseCode + " " + conn.getResponseMessage() + ". Cuerpo: " + errorBody);
        }
    }

    public void registerApoyoAcademico(Long courseId, String fileId, String academicSupportType, String token) throws IOException {
        if (fileId == null || fileId.trim().isEmpty()){
            throw new IllegalArgumentException("El fileId no puede ser nulo o vacío para registrar el apoyo académico.");
        }
        URL url = new URL(BASE_URL + "/courses/" + courseId + "/files/apoyos/" + fileId + "?tipo=" + URLEncoder.encode(academicSupportType, StandardCharsets.UTF_8.toString()));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setDoOutput(false); // No request body for this POST

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            String errorBody = "";
            if (conn.getErrorStream() != null) {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new IOException("Error al registrar apoyo académico: " + responseCode + " " + conn.getResponseMessage() + ". Cuerpo: " + errorBody);
        }
    }

    public void updateApoyoAcademicoTipo(Long courseId, String fileId, String nuevoTipo, String token) throws IOException {
        if (fileId == null || fileId.trim().isEmpty()){
            throw new IllegalArgumentException("El fileId no puede ser nulo o vacío para actualizar el tipo de apoyo académico.");
        }
        URL url = new URL(BASE_URL + "/courses/" + courseId + "/files/apoyos/" + fileId + "?tipo=" + URLEncoder.encode(nuevoTipo, StandardCharsets.UTF_8.toString()));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setDoOutput(false); // No request body for this PUT if data is in URL params

        int responseCode = conn.getResponseCode();
        // Consider HTTP_OK (200) or HTTP_NO_CONTENT (204) as success for PUT
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            String errorBody = "";
            if (conn.getErrorStream() != null) {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new IOException("Error al actualizar tipo de apoyo académico: " + responseCode + " " + conn.getResponseMessage() + ". Cuerpo: " + errorBody);
        }
    }

    public FileUploadInfoDTO getMicrocurriculumUploadUrl(Long courseId, String token) throws IOException {
        URL url = new URL(BASE_URL + "/courses/" + courseId + "/files/microcurriculos/upload-url");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String presignedUrlString = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("[CourseFileServiceFront] URL prefirmada para microcurrículo recibida del backend: " + presignedUrlString); // Diagnóstico
            String fileId = extractFileIdFromUrlPath(presignedUrlString);
            System.out.println("[CourseFileServiceFront] fileId extraído para microcurrículo: " + fileId); // Diagnóstico
            return new FileUploadInfoDTO(presignedUrlString, fileId);
        } else {
            String errorBody = "";
            if (conn.getErrorStream() != null) {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new IOException("Error al obtener URL de subida para microcurrículo: " + responseCode + " " + conn.getResponseMessage() + ". Cuerpo: " + errorBody);
        }
    }

    public void uploadFileToPresignedUrl(String presignedUrl, File file, String contentType, String token) throws IOException {
        URL url = new URL(presignedUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        try (OutputStream os = conn.getOutputStream()) {
            Files.copy(file.toPath(), os);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            String errorBody = "";
            if (conn.getErrorStream() != null) {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            } else if (conn.getInputStream() != null) {
                 errorBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new IOException("Error al subir archivo a URL prefirmada ('" + presignedUrl + "'): " + responseCode + " " + conn.getResponseMessage() + ". Cuerpo: " + errorBody);
        }
    }

    public String getApoyosUrl(Long courseId) {
        String endpoint = String.format("%s/courses/%d/files/apoyos", BASE_URL, courseId);
        try {
            java.net.URL url = new java.net.URL(endpoint);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String token = org.unisoftware.gestioncurricular.frontend.util.SessionManager.getInstance().getToken();
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                // Se espera un JSON con la lista de archivos de apoyo
                String json = new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                // Extraer la URL del primer archivo de apoyo (si existe)
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
                if (root.isArray() && root.size() > 0) {
                    com.fasterxml.jackson.databind.JsonNode primerArchivo = root.get(0);
                    if (primerArchivo.has("url")) {
                        return primerArchivo.get("url").asText();
                    }
                }
                return null; // No hay archivos de apoyo
            } else {
                String errorBody = "";
                if (conn.getErrorStream() != null) {
                    errorBody = new String(conn.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
                throw new RuntimeException("No se pudo obtener la lista de apoyos: " + errorBody);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la lista de apoyos: " + e.getMessage(), e);
        }
    }

    public String getMicrocurriculoUrl(Long courseId) {
        String endpoint = String.format("%s/courses/%d/files/microcurriculos/main", BASE_URL, courseId);
        try {
            java.net.URL url = new java.net.URL(endpoint);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String token = org.unisoftware.gestioncurricular.frontend.util.SessionManager.getInstance().getToken();
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                // Se espera que el backend retorne la URL como texto plano
                return new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } else {
                String errorBody = "";
                if (conn.getErrorStream() != null) {
                    errorBody = new String(conn.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
                throw new RuntimeException("No se pudo obtener la URL del microcurrículo: " + errorBody);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la URL del microcurrículo: " + e.getMessage(), e);
        }
    }

    public java.util.Map<String, String> getApoyosUrlsPorTipo(Long courseId) {
        String endpoint = String.format("%s/courses/%d/files/apoyos", BASE_URL, courseId);
        try {
            java.net.URL url = new java.net.URL(endpoint);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            String token = org.unisoftware.gestioncurricular.frontend.util.SessionManager.getInstance().getToken();
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                String json = new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
                java.util.Map<String, String> apoyosPorTipo = new java.util.HashMap<>();
                if (root.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode archivo : root) {
                        String tipo = archivo.has("tipo") ? archivo.get("tipo").asText() : "OTRO";
                        String urlArchivo = archivo.has("url") ? archivo.get("url").asText() : null;
                        if (urlArchivo != null && !urlArchivo.isBlank()) {
                            apoyosPorTipo.put(tipo, urlArchivo);
                        }
                    }
                }
                return apoyosPorTipo;
            } else {
                String errorBody = "";
                if (conn.getErrorStream() != null) {
                    errorBody = new String(conn.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
                throw new RuntimeException("No se pudo obtener la lista de apoyos: " + errorBody);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la lista de apoyos: " + e.getMessage(), e);
        }
    }
}
