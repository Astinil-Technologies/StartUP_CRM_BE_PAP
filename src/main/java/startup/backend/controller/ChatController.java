package startup.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import startup.backend.dto.ApiResponse;
import startup.backend.dto.MessageDto;
import startup.backend.dto.MessageResponseDto;
import startup.backend.dto.TypingIndicatorDto;
import startup.backend.entity.Message;
import startup.backend.service.MessageService;
import startup.backend.dto.TypingIndicatorDto;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat/messages")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;  // ✅ Injected WebSocket template

    // 🔹 Send a Message
    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(@RequestBody MessageDto messageDto) {
        MessageResponseDto response = messageService.sendMessage(messageDto);
        return ResponseEntity.ok(response);
    }

    // 🔹 Retrieve Chat History
    @GetMapping("/{conversationId}")
    public ResponseEntity<List<MessageResponseDto>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<MessageResponseDto> messages = messageService.getMessages(conversationId, PageRequest.of(page, size));
        return ResponseEntity.ok(messages);
    }

    // 🔹 Soft Delete
    @DeleteMapping("/{id}/soft")
    public ResponseEntity<String> softDelete(@PathVariable Long id,
                                             @RequestParam Long userId) throws AccessDeniedException {
        return ResponseEntity.ok(messageService.softDeleteMessage(id, userId));
    }

    // 🔹 Hard Delete
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<String> hardDelete(@PathVariable Long id,
                                             @RequestParam Long userId,
                                             @RequestParam(defaultValue = "false") boolean isAdmin) throws AccessDeniedException {
        return ResponseEntity.ok(messageService.hardDeleteMessage(id, userId, isAdmin));
    }

    // 🔹 Typing Indicator - WebSocket
    @MessageMapping("/typing")
    public void handleTypingIndicator(TypingIndicatorDto typingData) {
        if (messageService.isUserInConversation(typingData.getUserId(), typingData.getConversationId())) {
            typingData.setTimestamp(ZonedDateTime.now());

            messagingTemplate.convertAndSend(
                    "/topic/typing/" + typingData.getConversationId(),
                    typingData
            );
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<MessageResponseDto>> uploadFileMessage(
            @RequestParam Long senderId,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) Long groupId,
            @RequestParam String content,
            @RequestParam MultipartFile file
    ) throws IOException {
        MessageResponseDto savedMessage = messageService.sendMessageWithFile(senderId, recipientId, groupId, content, file);

        return ResponseEntity.ok(ApiResponse.success("File message sent successfully", savedMessage, 200));
    }


}
