package startup.backend.service;

import startup.backend.dto.TicketDto;
import startup.backend.entity.Ticket;

import java.util.List;

public interface TicketService {
    TicketDto createTicket(TicketDto ticketDto);
    List<TicketDto> getAllTickets();
    TicketDto getTicketById(Long id);
    List<TicketDto> getTicketsByUserId(Long userId);
    List<TicketDto> searchTickets(String query);
    TicketDto updateTicket(Long id, TicketDto ticketDto);
    void deleteTicket(Long id);
}
