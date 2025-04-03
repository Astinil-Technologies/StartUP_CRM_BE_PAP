package startup.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import startup.backend.dto.MessageDto;
import startup.backend.dto.MessageResponseDto;
import startup.backend.service.MessageService;

import java.util.List;

@RestController
@RequestMapping("/api/chat/messages")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    // Send a Message
    @PostMapping
    public ResponseEntity<MessageResponseDto> sendMessage(@RequestBody MessageDto messageDto) {
        MessageResponseDto response = messageService.sendMessage(messageDto);
        return ResponseEntity.ok(response); // Response properly formatted
    }

    // Retrieve Chat History
    @GetMapping("/{conversationId}")
    public ResponseEntity<List<MessageResponseDto>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<MessageResponseDto> messages = messageService.getMessages(conversationId, PageRequest.of(page, size));
        return ResponseEntity.ok(messages); // Response properly formatted
    }
}
