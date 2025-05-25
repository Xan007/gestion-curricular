package org.unisoftware.gestioncurricular.agent;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PromptBuilder {

    private static final Map<String, String> roleToPromptFile = Map.of(
            "docente", "prompts/docentePrompt.txt",
            "decano", "prompts/decanoPrompt.txt",
            "comite de programa", "prompts/comiteProgramaPrompt.txt",
            "director de programa", "prompts/directorProgramaPrompt.txt",
            "director de escuela", "prompts/directorEscuelaPrompt.txt"
    );

    public static String buildPrompt(String formattedRole) throws IOException {
        String globalPrompt = readResourceFile("prompts/globalPrompt.txt");

        String roleKey = formattedRole.toLowerCase().trim();
        String rolePromptFile = roleToPromptFile.getOrDefault(roleKey, "prompts/invitadoPrompt.txt");

        String rolePrompt = readResourceFile(rolePromptFile);

        return rolePrompt.replace("{globalPrompt}", globalPrompt);
    }

    private static String readResourceFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
