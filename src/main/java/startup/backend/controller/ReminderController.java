package startup.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.ReminderDto;
import startup.backend.entity.Reminder;
import startup.backend.entity.User;
import startup.backend.service.ReminderService;
import startup.backend.util.JwtTokenUtil;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;
    private final JwtTokenUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Reminder> createReminder(@RequestBody ReminderDto dto, HttpServletRequest request) {
        User user = jwtUtil.getCurrentUser(request);
        return ResponseEntity.ok(reminderService.createReminder(dto, user));
    }

    @GetMapping
    public ResponseEntity<List<Reminder>> getReminders(HttpServletRequest request) {
        User user = jwtUtil.getCurrentUser(request);
        return ResponseEntity.ok(reminderService.getReminders(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reminder> updateReminder(@PathVariable Long id, @RequestBody ReminderDto dto, HttpServletRequest request) {
        User user = jwtUtil.getCurrentUser(request);
        return ResponseEntity.ok(reminderService.updateReminder(id, dto, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id, HttpServletRequest request) {
        User user = jwtUtil.getCurrentUser(request);
        reminderService.deleteReminder(id, user);
        return ResponseEntity.noContent().build();
    }
}
