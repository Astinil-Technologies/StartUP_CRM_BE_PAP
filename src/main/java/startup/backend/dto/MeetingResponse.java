package startup.backend.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingResponse {

    // Common meeting details
    private Long id;
    private String title;
    private Instant scheduledFor;
    private String joinUrl;

}
