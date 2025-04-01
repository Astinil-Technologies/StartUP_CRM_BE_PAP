package startup.backend.service;


import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;
import startup.backend.dto.TicketDto;
import startup.backend.entity.Ticket;
import startup.backend.exception.ResourceNotFoundException;
import startup.backend.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;


    @Autowired
    private  ModelMapper modelMapper;

    // ✅ Convert DTO to Entity
    private Ticket convertToEntity(TicketDto ticketDTO) {
        return modelMapper.map(ticketDTO, Ticket.class);
    }

    // ✅ Convert Entity to DTO
    private TicketDto convertToDTO(Ticket ticket) {
        return modelMapper.map(ticket, TicketDto.class);
    }

    @Override
    public TicketDto createTicket(TicketDto ticketDto) {
          Ticket ticket = new Ticket();

          ticket =convertToEntity(ticketDto);

        if (ticket.getTitle() == null || ticket.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }

        ticket.setStatus(Ticket.Status.OPEN);

        return convertToDTO(ticketRepository.save(ticket));
    }

    @Override
    public List<TicketDto> getAllTickets() {
        List<Ticket>tickets = ticketRepository.findAll();
        List<TicketDto>ticketDto=tickets.stream().map(ticket -> this.convertToDTO(ticket)).collect(Collectors.toList());
        return ticketDto;
    }

    @Override
    public List<TicketDto> getTicketsByUserId(Long userId) {

        List<Ticket>tickets = ticketRepository.findByAssignedUserId(userId);
        List<TicketDto>ticketDto=tickets.stream().map(ticket -> this.convertToDTO(ticket)).collect(Collectors.toList());
        return ticketDto;

    }

    @Override
    public List<TicketDto> searchTickets(String query) {

        List<Ticket>tickets = ticketRepository.searchTickets(query);
        List<TicketDto>ticketDto=tickets.stream().map(ticket -> this.convertToDTO(ticket)).collect(Collectors.toList());
        return ticketDto;
    }

    @Override
    public TicketDto getTicketById(Long id) {

          Ticket ticket = ticketRepository.findById(id)
                  .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));
        return convertToDTO(ticket);
    }

    @Override
    public TicketDto updateTicket(Long id, TicketDto updatedTicket) {

        TicketDto existingTicket = getTicketById(id);

        if (updatedTicket.getTitle() != null) {
            existingTicket.setTitle(updatedTicket.getTitle());
        }
        if (updatedTicket.getDescription() != null) {
            existingTicket.setDescription(updatedTicket.getDescription());
        }
        if (updatedTicket.getPriority() != null) {
            existingTicket.setPriority(updatedTicket.getPriority());
        }
        if (updatedTicket.getStatus() != null) {
            existingTicket.setStatus(updatedTicket.getStatus());
        }


          Ticket t =convertToEntity(existingTicket);
           ticketRepository.save(t);

           return convertToDTO(t);



    }

    @Override
    public void deleteTicket(Long id) {
        TicketDto ticket = getTicketById(id);
        Ticket t =convertToEntity(ticket);
        ticketRepository.delete(t);
    }
}
