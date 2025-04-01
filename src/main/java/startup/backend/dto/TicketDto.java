package startup.backend.dto;

import lombok.Data;
import startup.backend.entity.Ticket;

@Data
public class TicketDto {
    private String title;
    private String description;
    private Ticket.Priority priority;
    private Ticket.Status status;
    private Long assignedUserId;
}
