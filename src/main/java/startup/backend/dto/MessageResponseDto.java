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


    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;

    public MessageResponseDto(Long id, Long id1, Long recipientId, String content, String format, String fileName, String fileType, String fileUrl, Long fileSize) {
    this.messageId = id;
    this.senderId = id1;
    this.recipientId = recipientId;
    this.content = content;

    this.timestamp = format;
    this.fileName = fileName;
    this.fileType = fileType;
    this.fileUrl = fileUrl;
    this.fileSize = fileSize;

    }
}
