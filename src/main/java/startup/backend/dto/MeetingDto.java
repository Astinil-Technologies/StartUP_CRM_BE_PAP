package startup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingDto {
    private Long id;
    private String meetingId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long hostId;
    private Set<Long> participantIds;
    private boolean locked;
    private boolean started;
    private boolean lobbyEnabled;
}
