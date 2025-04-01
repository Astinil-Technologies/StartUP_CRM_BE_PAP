package startup.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.TicketDto;
import startup.backend.entity.Ticket;
import startup.backend.service.TicketService;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping("/createTicket")
    public TicketDto createTicket(@RequestBody TicketDto ticket) {
        return ticketService.createTicket(ticket);
    }

    @GetMapping("/getAllTickets")
    public List<TicketDto> getAllTickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String query) {

        if (userId != null) {
            return ticketService.getTicketsByUserId(userId);
        } else if (query != null) {
            return ticketService.searchTickets(query);
        }
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public TicketDto getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @GetMapping(params = "query")
    public List<TicketDto> searchTickets(@RequestParam String query) {
        return ticketService.searchTickets(query);
    }

    @PutMapping("/{id}")
    public TicketDto updateTicket(@PathVariable Long id, @RequestBody TicketDto ticket) {
        return ticketService.updateTicket(id, ticket);
    }

    @DeleteMapping("/{id}")
    public String deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return "Ticket deleted successfully!";
    }
}