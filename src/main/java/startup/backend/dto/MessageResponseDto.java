package startup.backend.dto;

import lombok.Data;

@Data
public class MessageResponseDto {

    private Long messageId;
    private Long senderId;
    private Long recipientId;
    private String content;
    private String timestamp;

    public MessageResponseDto(Long messageId, Long senderId, Long recipientId, String content, String timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.timestamp = timestamp;
    }
}
