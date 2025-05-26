package org.unisoftware.gestioncurricular.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unisoftware.gestioncurricular.agent.PromptBuilder;
import org.unisoftware.gestioncurricular.agent.tools.CourseTools;
import org.unisoftware.gestioncurricular.agent.tools.ProgramTools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final SystemPromptTemplate systemPromptTemplate;
    private final ProgramTools programTools;
    private final CourseTools courseTools;

    @Autowired
    public ChatService(ChatModel chatModel, ProgramTools programTools, CourseTools courseTools) throws IOException {
        this.programTools = programTools;
        this.courseTools = courseTools;

        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(5)
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        String promptBody = PromptBuilder.buildPrompt("{{role}}");
        this.systemPromptTemplate = new SystemPromptTemplate(promptBody);
    }

    public String generateResponse(String userInput, String role, String conversationId) throws IOException {
        SystemMessage systemMessage = (SystemMessage) systemPromptTemplate.createMessage(Map.of("role", role));
        List<Message> promptMessages = List.of(systemMessage, new UserMessage(userInput));
        Prompt prompt = new Prompt(promptMessages);

        var clientRequest = chatClient.prompt(prompt).tools(programTools, courseTools);

        if (conversationId != null) {
            clientRequest = clientRequest.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId));
        }

        return clientRequest.call().content();
    }
}
