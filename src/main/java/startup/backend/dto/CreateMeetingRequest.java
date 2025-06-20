package startup.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMeetingRequest {

    @NotBlank(message = "Title is required")
    private String title;

    // Optional; if null => start now
    @Future(message = "Scheduled time must be in the future")
    private Instant scheduledFor;
}
