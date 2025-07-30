package startup.backend.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import startup.backend.dto.ChatMessageDTO;
import startup.backend.entity.User;
import startup.backend.repository.UserRepository;
import startup.backend.service.ChatService;

import java.security.Principal;

@Controller
public class ChatWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private UserRepository userRepository;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.userRepository = userRepository;
    }


    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO messageDTO, Principal principal) {
        if (principal == null) {
            System.out.println("❌ Principal is null");
            return;
        }

        String senderUsername = principal.getName(); // Authenticated
        if (!senderUsername.equals(messageDTO.getSenderUsername())) {
            System.out.println("❌ Sender mismatch: " + senderUsername + " vs " + messageDTO.getSenderUsername());
            return;
        }

        System.out.println("💬 WebSocket received: " + messageDTO);
        chatService.saveMessage(messageDTO);

        messagingTemplate.convertAndSendToUser(
                messageDTO.getReceiverId(),
                "/queue/messages",
                messageDTO
        );
    }
}