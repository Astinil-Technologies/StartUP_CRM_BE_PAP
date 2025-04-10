package startup.backend.dto;



import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class TypingIndicatorDto {
    private Long userId;
    private Long conversationId;
    private boolean isTyping;
    private ZonedDateTime timestamp;
}
