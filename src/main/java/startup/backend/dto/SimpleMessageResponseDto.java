package startup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleMessageResponseDto {
    private Long messageId;
    private Long senderId;
    private Long recipientId;
    private String content;
    private String timestamp;  // 🔁 Changed from ZonedDateTime to String
}
