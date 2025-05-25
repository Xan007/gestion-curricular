package org.unisoftware.gestioncurricular.frontend.service;

import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.frontend.util.SessionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper; // Asegúrate de tener esta dependencia
import java.util.Map;

@Service
public class ChatServiceFront {

    private static final String CHAT_API_URL = "http://localhost:8080/ai/generate";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String sendMessage(String message) throws Exception {
        String token = SessionManager.getInstance().getToken();


        // El backend espera el mensaje como un parámetro de consulta llamado "message"
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        URL url = new URL(CHAT_API_URL + "?message=" + encodedMessage);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET"); // Cambiado a GET según el ChatController del backend
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/json");
        // No se necesita Content-Type ni DoOutput para una solicitud GET sin cuerpo

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // Parsear la respuesta JSON para extraer el contenido del mensaje
                Map<String, Object> jsonResponse = objectMapper.readValue(response.toString(), Map.class);
                if (jsonResponse.containsKey("response")) {
                    return (String) jsonResponse.get("response");
                } else if (jsonResponse.containsKey("error")) {
                    throw new RuntimeException("Error from AI service: " + jsonResponse.get("error"));
                } else {
                    throw new RuntimeException("Unexpected response format from AI service.");
                }
            }
        } else {
            String errorMessage;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String errorLine;
                while ((errorLine = br.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
                errorMessage = errorResponse.toString();
            } catch (Exception e) {
                errorMessage = "HTTP error code: " + responseCode;
            }
            System.err.println("Error response from server: " + errorMessage);
            throw new RuntimeException("Failed to get response from AI service. HTTP error code: " + responseCode + ". Message: " + errorMessage);
        }
    }
}

