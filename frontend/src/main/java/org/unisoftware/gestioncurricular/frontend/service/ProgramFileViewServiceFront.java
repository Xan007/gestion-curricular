package org.unisoftware.gestioncurricular.frontend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;

@Service
public class ProgramFileViewServiceFront {
    private final RestTemplate restTemplate = new RestTemplate();

    public String getResultadosUrl(Long programId) {
        String endpoint = String.format("http://localhost:8080/programas/%d/files/results/main", programId);
        HttpHeaders headers = new HttpHeaders();
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isBlank()) {
            headers.set("Authorization", "Bearer " + token);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(endpoint, org.springframework.http.HttpMethod.GET, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("No se pudo obtener la URL del resultado de aprendizaje: " + response.getBody());
        }
        return response.getBody();
    }

    public String getCurriculumsUrl(Long programId) {
        String endpoint = String.format("http://localhost:8080/programas/%d/files/curriculums/main", programId);
        HttpHeaders headers = new HttpHeaders();
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isBlank()) {
            headers.set("Authorization", "Bearer " + token);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(endpoint, org.springframework.http.HttpMethod.GET, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("No se pudo obtener la URL del curriculum: " + response.getBody());
        }
        return response.getBody();
    }
}

