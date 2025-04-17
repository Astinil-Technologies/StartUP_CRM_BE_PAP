package startup.backend.controller;

import io.jsonwebtoken.Jwt;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.TicketDto;
import startup.backend.service.TicketService;
import startup.backend.util.JwtTokenUtil;

import java.util.List;
import java.util.Map;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;




    @PostMapping("/createTicket")
    public TicketDto createTicket(@RequestBody TicketDto ticket) {
        System.out.println("createticket is called");
        return ticketService.createTicket(ticket);
    }




    @GetMapping("/getAllTickets")
    public Page<TicketDto> getAllTickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if(query != null){

            return ticketService.searchTickets(query, pageable);
        }
       else  if (userId != null) {
            return ticketService.getTicketsByUserId(userId, pageable);
        } else if (query != null) {
            return ticketService.searchTickets(query, pageable);
        }

        return ticketService.getAllTickets(pageable);

    }


    @GetMapping("/{id}")
    public TicketDto getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }

    @PutMapping("/{id}")
    public TicketDto updateTicket(@PathVariable Long id, @RequestBody TicketDto ticket) {
        System.out.println("update called");
        return ticketService.updateTicket(id, ticket);
    }

    @DeleteMapping("/{id}")
    public String deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return "Ticket deleted successfully!";
    }
}