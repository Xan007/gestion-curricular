package org.unisoftware.gestioncurricular.frontend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.unisoftware.gestioncurricular.frontend.dto.ProposalFileDTO;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ProposalFileServiceFront {

    private final ObjectMapper objectMapper = new ObjectMapper();
    // TODO: Mover BASE_URL a un archivo de configuración o una clase de constantes
    private static final String BASE_URL = "http://localhost:8080";

    public ProposalFileDTO getUploadUrl(Long courseId, Long proposalId, String token) throws IOException {
        URL url = new URL(BASE_URL + "/proposals/" + courseId + "/files/" + proposalId + "/upload-url");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(responseBody, ProposalFileDTO.class);
        } else {
            String errorBody = "";
            if (conn.getErrorStream() != null) {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new IOException("Error al obtener URL de subida: " + responseCode + " " + conn.getResponseMessage() + ". Cuerpo: " + errorBody);
        }
    }

    public void uploadFileToPresignedUrl(String presignedUrl, File file, String contentType, String token) throws IOException {
        URL url = new URL(presignedUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);

        // Añadir la cabecera Authorization si el token está disponible y la URL es de Supabase (o siempre, por si acaso)
        // El error específico lo pide para Supabase.
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        try (OutputStream os = conn.getOutputStream()) {
            Files.copy(file.toPath(), os);
        }

        int responseCode = conn.getResponseCode();
        // S3 devuelve 200 OK en subidas PUT exitosas a URLs prefirmadas
        if (responseCode != HttpURLConnection.HTTP_OK) {
            String errorBody = "";
            // Intentar leer el cuerpo del error, S3 puede devolver XML con detalles
            if (conn.getErrorStream() != null) {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            } else if (conn.getInputStream() != null) { // A veces la info está en el input stream
                 errorBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new IOException("Error al subir archivo a URL prefirmada ('" + presignedUrl + "'): " + responseCode + " " + conn.getResponseMessage() + ". Cuerpo: " + errorBody);
        }
    }

    public ProposalFileDTO getDisplayUrlDTO(Long courseId, Long proposalId, String token) throws IOException {
        URL url = new URL(BASE_URL + "/proposals/" + courseId + "/files/" + proposalId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return objectMapper.readValue(responseBody, ProposalFileDTO.class);
        } else {
            String errorBody = "";
            if (conn.getErrorStream() != null) {
                errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new IOException("Error al obtener URL de visualización del archivo: " + responseCode + " " + conn.getResponseMessage() + ". Cuerpo: " + errorBody);
        }
    }
}
