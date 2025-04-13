package startup.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import startup.backend.enums.ChatNotificationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_notifications")
@Getter
@Setter
public class ChatNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private Long messageId;
    private Long senderId;
    private Long recipientId;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private ChatNotificationStatus status;

    // Constructors
    public ChatNotification() {}

    public ChatNotification(Long messageId, Long senderId, Long recipientId, LocalDateTime timestamp, ChatNotificationStatus status) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters & Setters
    // (Generated or use Lombok if preferred)
}