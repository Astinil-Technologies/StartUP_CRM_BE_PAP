package startup.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.MarkReadRequest;
import startup.backend.entity.ChatNotification;
import startup.backend.service.ChatNotificationService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class ChatNotificationController {

    private final ChatNotificationService notificationService;

    // ✅ Get unread notifications by userId
    @GetMapping("/unread")
    public ResponseEntity<List<ChatNotification>> getUnreadNotifications(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<ChatNotification> unread = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(unread);
    }

    // ✅ Mark notifications as read
    @PostMapping("/mark-read")
    public ResponseEntity<String> markNotificationsAsRead(@RequestBody MarkReadRequest request) {
        if (request.getUserId() == null || request.getNotificationIds() == null || request.getNotificationIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request payload.");
        }

        notificationService.markNotificationsAsRead(request.getUserId(), request.getNotificationIds());
        return ResponseEntity.ok("Notifications marked as read successfully.");
    }
}
