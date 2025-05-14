package startup.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReminderDto {
    private String title;
    private LocalDateTime dueDateTime;
    private Integer notifyBeforeMinutes;
    private String attachmentPath;
}