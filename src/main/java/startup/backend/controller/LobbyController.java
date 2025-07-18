package startup.backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import startup.backend.service.LobbyService;

import java.util.List;

@RestController
@RequestMapping("/lobby")
public class LobbyController {
    @Autowired
    private LobbyService lobbyService;

    @PostMapping("/{meetingId}/request-entry")
    public String requestEntry(@PathVariable String meetingId, @RequestParam Long userId) {
        lobbyService.addToLobby(meetingId, userId);
        return "User added to waiting room.";
    }

    @PostMapping("/{meetingId}/approve")
    public String approveUser(@PathVariable String meetingId, @RequestParam Long userId) {
        lobbyService.approveEntry(meetingId, userId);
        return "User approved and joined meeting.";
    }

    @GetMapping("/{meetingId}/queue")
    public List<Long> getWaitingUsers(@PathVariable String meetingId) {
        return lobbyService.getLobbyUsers(meetingId);
    }
}
