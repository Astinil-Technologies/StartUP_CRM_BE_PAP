package startup.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import startup.backend.dto.CreateMeetingRequest;
import startup.backend.dto.JoinMeetingRequest;
import startup.backend.dto.MeetingResponse;
import startup.backend.dto.ShareScreenTokenResponse;
import startup.backend.service.MeetingService;
import startup.backend.service.ShareScreenTokenService;

import java.time.Instant;

@RestController
@RequestMapping("/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final ShareScreenTokenService tokenService;

    /* POST /meeting/new */
    @PostMapping("/new")
    public ResponseEntity<MeetingResponse> start(
            @Valid @RequestBody CreateMeetingRequest request
            ) {
        String userId = getCurrentUserId();
        MeetingResponse resp = meetingService.start(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /* POST /meeting/join */
    @PostMapping("/join")
    public ResponseEntity<MeetingResponse> join(
            @Valid @RequestBody JoinMeetingRequest request) {
        return ResponseEntity.ok(meetingService.join(request));
    }

    /* POST /meeting/schedule */
    @PostMapping("/schedule")
    public ResponseEntity<MeetingResponse> schedule(
            @Valid @RequestBody CreateMeetingRequest request
    ) {
        String userId = getCurrentUserId();
        MeetingResponse resp = meetingService.schedule(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /* POST /meeting/schedule */
    @PostMapping("/{id}/end")
    public ResponseEntity<MeetingResponse> endMeeting(@PathVariable Long id) {
        String userId = getCurrentUserId();
        MeetingResponse ended = meetingService.endMeeting(id, userId);
        return ResponseEntity.ok(ended);                 // 200 OK with the updated state
    }

    /* GET /meeting/share-screen-token?meetingId=... */
    @GetMapping("/share-screen-token")
    public ResponseEntity<ShareScreenTokenResponse> token(
            @RequestParam Long meetingId
    ) {
        String userId = getCurrentUserId();
        String jwt = tokenService.generate(meetingId, userId);
        return ResponseEntity.ok(ShareScreenTokenResponse.builder()
                .token(jwt)
                .expiresAt(Instant.now().plusSeconds(300))
                .build());
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
    }
}

