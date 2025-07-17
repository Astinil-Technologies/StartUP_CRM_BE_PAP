package startup.backend.controller;



import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.MeetingDto;
import startup.backend.service.MeetingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;


    @PostMapping
    public ResponseEntity<MeetingDto> createMeeting(@RequestBody MeetingDto dto) {
        return ResponseEntity.ok(meetingService.createMeeting(dto));
    }


    @PostMapping("/{meetingId}/join")
    public ResponseEntity<MeetingDto> joinMeeting(@PathVariable String meetingId, @RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.joinMeeting(meetingId, userId));
    }


    @GetMapping("/upcoming")
    public ResponseEntity<List<MeetingDto>> getUpcomingMeetings(@RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.getUpcomingMeetings(userId));
    }


    @GetMapping("/history")
    public ResponseEntity<List<MeetingDto>> getPastMeetings(@RequestParam Long userId) {
        return ResponseEntity.ok(meetingService.getPastMeetings(userId));
    }


    @PutMapping("/{id}")
    public ResponseEntity<MeetingDto> updateMeeting(@PathVariable Long id, @RequestBody MeetingDto dto) {
        return ResponseEntity.ok(meetingService.updateMeeting(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{meetingId}/lock")
    public ResponseEntity<Void> lockMeeting(@PathVariable String meetingId) {
        meetingService.lockMeeting(meetingId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{meetingId}/kick")
    public ResponseEntity<Void> kickUser(@PathVariable String meetingId, @RequestParam Long userId) {
        meetingService.kickUser(meetingId, userId);
        return ResponseEntity.ok().build();
    }
}