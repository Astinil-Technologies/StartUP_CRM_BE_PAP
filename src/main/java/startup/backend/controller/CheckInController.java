package startup.backend.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.CheckInDto;
import startup.backend.dto.StatusDto;
import startup.backend.service.CheckInService;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {
    private final CheckInService checkInService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CheckInDto> checkIn() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(checkInService.checkIn(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StatusDto> getStatus() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(checkInService.getStatus(userId));
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
    }
}