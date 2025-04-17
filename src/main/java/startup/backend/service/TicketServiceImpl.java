package startup.backend.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import startup.backend.dto.TicketDto;
import startup.backend.entity.Ticket;
import startup.backend.repository.TicketRepository;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    public TicketServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public TicketDto createTicket(TicketDto ticketDto) {
        Ticket ticket = Ticket.builder()
                .title(ticketDto.getTitle())
                .description(ticketDto.getDescription())
                .priority(ticketDto.getPriority())
                .status(ticketDto.getStatus())
                .assignedUserId(ticketDto.getAssignedUserId())
                .createdBy(ticketDto.getCreatedBy())
                .build();

        ticket.setStatus(Ticket.Status.valueOf("OPEN"));

        ticket = ticketRepository.save(ticket);
        return mapToDto(ticket);
    }

    @Override
    public Page<TicketDto> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::mapToDto);
    }


    @Override
    public TicketDto getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return mapToDto(ticket);
    }

    @Override
    public Page<TicketDto> getTicketsByUserId(Long userId, Pageable pageable) {
        return ticketRepository.findByCreatedBy(userId, pageable).map(this::mapToDto);
    }

    @Override
    public Page<TicketDto> searchTickets(String query, Pageable pageable) {

        return ticketRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query, pageable)
                .map(this::mapToDto);
    }

    @Override
    public TicketDto updateTicket(Long id, TicketDto ticketDto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setTitle(ticketDto.getTitle());
        ticket.setDescription(ticketDto.getDescription());
        ticket.setPriority(ticketDto.getPriority());
        ticket.setStatus(ticketDto.getStatus());
        ticket.setAssignedUserId(ticketDto.getAssignedUserId());

        return mapToDto(ticketRepository.save(ticket));
    }

    @Override
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    private TicketDto mapToDto(Ticket ticket) {
        return new TicketDto(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssignedUserId(),
                ticket.getCreatedBy(),
                ticket.getCreatedTimestamp()
        );
    }
}