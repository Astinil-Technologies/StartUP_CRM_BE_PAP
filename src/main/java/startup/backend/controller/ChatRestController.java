package startup.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import startup.backend.entity.ChatMessage;
import startup.backend.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_INSTRUCTOR')")
    public List<ChatMessage> getChatHistory(@RequestParam String user1, @RequestParam String user2) {
        return chatService.getChatHistory(user1, user2);
    }

}