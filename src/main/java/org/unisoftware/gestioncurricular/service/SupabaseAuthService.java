package org.unisoftware.gestioncurricular.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.config.SupabaseProperties;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.BodyPublishers.ofString;

@Service
public class SupabaseAuthService {

    private final HttpClient http = HttpClient.newHttpClient();
    private final SupabaseProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    public SupabaseAuthService(SupabaseProperties properties) {
        this.properties = properties;
    }

    public String signUp(String email, String password) throws Exception {
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(properties.getUrl() + "/auth/v1/signup"))
                .header("apikey", properties.getAnonKey())
                .header("Content-Type", "application/json")
                .POST(ofString(json))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Signup failed: " + response.body());
        }

        JsonNode node = mapper.readTree(response.body());
        return node.get("access_token").asText();
    }

    public String signIn(String email, String password) throws Exception {
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(properties.getUrl() + "/auth/v1/token?grant_type=password"))
                .header("apikey", properties.getAnonKey())
                .header("Content-Type", "application/json")
                .POST(ofString(json))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Signin failed: " + response.body());
        }

        JsonNode node = mapper.readTree(response.body());
        return node.get("access_token").asText();
    }
}
