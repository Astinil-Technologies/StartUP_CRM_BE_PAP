package startup.backend.service;

import startup.backend.dto.TicketDto;
import startup.backend.entity.Ticket;

import java.util.List;

public interface TicketService {

    TicketDto createTicket(TicketDto ticketDto);

    List<TicketDto> getAllTickets();

    List<TicketDto> getTicketsByUserId(Long userId);

    List<TicketDto> searchTickets(String query);

    TicketDto getTicketById(Long id);

    TicketDto updateTicket(Long id, TicketDto ticket);

    void deleteTicket(Long id);
}
