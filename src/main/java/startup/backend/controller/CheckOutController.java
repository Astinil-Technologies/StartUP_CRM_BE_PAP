package startup.backend.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.CheckOutDto;
import startup.backend.dto.CheckOutStatusDto;
import startup.backend.service.CheckOutService;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckOutController {
    private final CheckOutService checkOutService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CheckOutDto> checkOut() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(checkOutService.checkOut(userId));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CheckOutStatusDto> getCheckOutStatus() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(checkOutService.getCheckOutStatus(userId));
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
    }
}
