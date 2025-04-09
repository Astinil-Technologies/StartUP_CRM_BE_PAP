package startup.backend.dto;

import lombok.*;
import startup.backend.entity.Ticket;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDto {
    private Long id;
    private String title;
    private String description;
    private Ticket.Priority priority;
    private Ticket.Status status;
    private Long assignedUserId;
    private Long createdBy;
    private LocalDateTime createdTimestamp;

}