package startup.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import startup.backend.dto.ChatMessageDTO;
import startup.backend.entity.ChatMessage;
//import startup.backend.enums.MessageStatus;
import startup.backend.repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatMessageDTO dto) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(dto.getSenderUsername()); // Username used as ID
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent());
        message.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getChatHistory(String user1, String user2) {
        return chatMessageRepository.findChatBetweenUsers(user1, user2, user2, user1);
    }

}