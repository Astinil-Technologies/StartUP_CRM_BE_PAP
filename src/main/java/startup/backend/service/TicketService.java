package startup.backend.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import startup.backend.dto.TicketDto;
import startup.backend.entity.Ticket;

import java.util.List;

public interface TicketService {
//    TicketDto createTicket(TicketDto ticketDto);
//    List<TicketDto> getAllTickets();
//    TicketDto getTicketById(Long id);
//    List<TicketDto> getTicketsByUserId(Long userId);
//    List<TicketDto> searchTickets(String query);
//    TicketDto updateTicket(Long id, TicketDto ticketDto);
//    void deleteTicket(Long id);


    TicketDto createTicket(TicketDto ticketDto);
    Page<TicketDto> getAllTickets(Pageable pageable);
    TicketDto getTicketById(Long id);
    Page<TicketDto> getTicketsByUserId(Long userId, Pageable pageable);
    Page<TicketDto> searchTickets(String query, Pageable pageable);

    TicketDto updateTicket(Long id, TicketDto ticketDto);
    void deleteTicket(Long id);
}
