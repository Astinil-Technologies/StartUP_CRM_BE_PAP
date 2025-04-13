package startup.backend.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MarkReadRequest {
    private Long userId;
    private List<Long> notificationIds;

    // Getters and Setters
}