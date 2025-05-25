package org.unisoftware.gestioncurricular.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.agent.tools.ProgramTools;
import org.unisoftware.gestioncurricular.entity.AuthUser;
import org.unisoftware.gestioncurricular.entity.UserDetails;
import org.unisoftware.gestioncurricular.repository.AuthUserRepository;
import org.unisoftware.gestioncurricular.security.util.SecurityUtil;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final AuthUserRepository authUserRepository;
    private final ProgramTools programTools;
    private SystemPromptTemplate systemPromptTemplate;

    @Autowired
    public ChatController(OpenAiChatModel chatModel, AuthUserRepository authUserRepository, ProgramTools programTools) throws IOException {
        this.authUserRepository = authUserRepository;
        this.programTools = programTools;

        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(5)
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        ClassPathResource resource = new ClassPathResource("prompts/systemPrompt.txt");
        String systemPromptText = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        this.systemPromptTemplate = new SystemPromptTemplate(systemPromptText);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestParam String message) {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "ID de usuario no encontrado."));
        }

        String role = formatRole(SecurityUtil.getCurrentUserRole());

        String conversationId = userId.toString();

        SystemMessage systemMessage = (SystemMessage) this.systemPromptTemplate.createMessage(
                Map.of("role", role)
        );
        System.out.println(systemMessage.getText());

        List<Message> userMessagesHistory = chatMemory.get(conversationId);
        List<Message> promptMessages = List.of(systemMessage, new UserMessage(message));

        Prompt prompt = new Prompt(promptMessages);

        String response = chatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(programTools)
                .call()
                .content();

        return ResponseEntity.ok(Map.of("response", response));
    }

    private String nonNull(String value) {
        return value != null ? value : "";
    }

    private String formatRole(String rawRole) {
        if (rawRole == null || !rawRole.startsWith("ROLE_")) {
            return "Invitado";
        }
        String formatted = rawRole.substring(5).toLowerCase().replace("_", " ");
        String[] words = formatted.split(" ");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return capitalized.toString().trim();
    }
}
