package org.unisoftware.gestioncurricular.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.unisoftware.gestioncurricular.security.util.SecurityUtil;
import org.unisoftware.gestioncurricular.service.ChatService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestParam String message) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String formattedRole = SecurityUtil.formatRole(SecurityUtil.getCurrentUserRole());
        String conversationId = userId != null ? userId.toString() : null;

        try {
            String response = chatService.generateResponse(message, formattedRole, conversationId);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
