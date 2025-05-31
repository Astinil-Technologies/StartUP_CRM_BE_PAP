package startup.backend.dto;

import lombok.Data;
import startup.backend.enums.RecurringType;
import java.time.LocalDateTime;
@Data
public class ReminderDto {
    private String title;
    private LocalDateTime dueDateTime;
    private Integer notifyBeforeMinutes;
    private String attachmentPath;
    private Boolean recurring;
    private RecurringType recurringType;
    private Boolean notified;
}