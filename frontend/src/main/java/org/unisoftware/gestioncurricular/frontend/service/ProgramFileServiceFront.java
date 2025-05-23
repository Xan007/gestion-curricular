package org.unisoftware.gestioncurricular.frontend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

@Service
public class ProgramFileServiceFront {
    private final RestTemplate restTemplate = new RestTemplate();


    public void uploadCurriculum(Long programId, File file, String dateStr) throws IOException {
        String url = getUploadUrl(programId, "curriculums", dateStr);
        uploadFileToUrl(url, file);
    }

    public void uploadResultados(Long programId, File file, String dateStr) throws IOException {
        String url = getUploadUrl(programId, "results", dateStr);
        uploadFileToUrl(url, file);
    }

    private String getUploadUrl(Long programId, String tipo, String dateStr) {
        String endpoint = String.format("http://localhost:8080/programas/%d/files/%s/upload-url", programId, tipo);
        if (dateStr != null && !dateStr.isBlank()) {
            endpoint += "?dateStr=" + dateStr;
        }
        HttpHeaders headers = new HttpHeaders();
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isBlank()) {
            headers.set("Authorization", "Bearer " + token);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(endpoint, org.springframework.http.HttpMethod.GET, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("No se pudo obtener la URL de subida: " + response.getBody());
        }
        return Objects.requireNonNull(response.getBody());
    }

    private void uploadFileToUrl(String url, File file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        String mimeType = Files.probeContentType(file.toPath());
        headers.setContentType(MediaType.parseMediaType(mimeType != null ? mimeType : "application/octet-stream"));
        // No se agregan headers apikey ni Authorization, solo el Content-Type
        byte[] fileBytes;
        try (FileInputStream fis = new FileInputStream(file)) {
            fileBytes = fis.readAllBytes();
        }
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.POST, requestEntity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al subir archivo: " + response.getBody());
        }
    }
}

