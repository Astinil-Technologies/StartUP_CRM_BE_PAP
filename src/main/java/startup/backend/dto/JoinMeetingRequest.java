package startup.backend.dto;

import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinMeetingRequest {

    @NotNull(message = "Meeting ID is required")
    private Long meetingId;
}

