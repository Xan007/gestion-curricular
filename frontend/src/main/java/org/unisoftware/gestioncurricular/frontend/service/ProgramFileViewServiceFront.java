package org.unisoftware.gestioncurricular.frontend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ProgramFileViewServiceFront {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public List<String> getHistoricalResultadosUrls(Long programId) {
        try {
            String endpoint = String.format("http://localhost:8080/programas/%d/files/results", programId);
            HttpHeaders headers = new HttpHeaders();
            String token = SessionManager.getInstance().getToken();
            if (token != null && !token.isBlank()) {
                headers.set("Authorization", "Bearer " + token);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.err.println("Error en la respuesta del servidor: " + response.getStatusCode());
                return new ArrayList<>();
            }

            String responseBody = response.getBody();
            System.out.println("Respuesta del servidor (resultados históricos): " + responseBody);

            try {
                // Deserializar como lista de objetos y extraer solo el campo url
                List<Map<String, Object>> objetos = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {});
                List<String> urls = new ArrayList<>();
                for (Map<String, Object> obj : objetos) {
                    Object urlObj = obj.get("url");
                    if (urlObj != null) {
                        urls.add(urlObj.toString());
                    }
                }
                return urls;
            } catch (Exception e) {
                System.err.println("Error al deserializar la respuesta a List<Map<String, Object>>: " + e.getMessage());

                // Intentos alternativos de deserialización
                if (responseBody.startsWith("\"") && responseBody.endsWith("\"")) {
                    List<String> singleItemList = new ArrayList<>();
                    singleItemList.add(responseBody.substring(1, responseBody.length() - 1));
                    return singleItemList;
                }

                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error general al obtener URLs históricas de resultados: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> getHistoricalCurriculumsUrls(Long programId) {
        try {
            String endpoint = String.format("http://localhost:8080/programas/%d/files/curriculums", programId);
            HttpHeaders headers = new HttpHeaders();
            String token = SessionManager.getInstance().getToken();
            if (token != null && !token.isBlank()) {
                headers.set("Authorization", "Bearer " + token);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.err.println("Error en la respuesta del servidor: " + response.getStatusCode());
                return new ArrayList<>();
            }

            String responseBody = response.getBody();
            System.out.println("Respuesta del servidor (currículums históricos): " + responseBody);

            try {
                // Deserializar como lista de objetos y extraer solo el campo url
                List<Map<String, Object>> objetos = objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {});
                List<String> urls = new ArrayList<>();
                for (Map<String, Object> obj : objetos) {
                    Object urlObj = obj.get("url");
                    if (urlObj != null) {
                        urls.add(urlObj.toString());
                    }
                }
                return urls;
            } catch (Exception e) {
                System.err.println("Error al deserializar la respuesta a List<Map<String, Object>>: " + e.getMessage());

                // Intentos alternativos de deserialización
                if (responseBody.startsWith("\"") && responseBody.endsWith("\"")) {
                    List<String> singleItemList = new ArrayList<>();
                    singleItemList.add(responseBody.substring(1, responseBody.length() - 1));
                    return singleItemList;
                }

                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error general al obtener URLs históricas de currículos: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
