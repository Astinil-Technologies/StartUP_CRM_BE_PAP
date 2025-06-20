package startup.backend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import startup.backend.dto.CreateMeetingRequest;
import startup.backend.dto.JoinMeetingRequest;
import startup.backend.dto.MeetingResponse;
import startup.backend.entity.Meeting;
import startup.backend.repository.MeetingRepository;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository repo;

    /**
     * Starts a new meeting for the user.
     *
     * @param req    the request containing meeting details.
     * @param userId the ID of the user starting the meeting.
     * @return a response containing meeting details.
     */
    @Override
    public MeetingResponse start(CreateMeetingRequest req, String userId) {
        // Check if the user is already in a meeting
        if (repo.existsByHostUserIdAndEndedFalse(userId)) {
            throw new RuntimeException("User is already in a meeting");
        }

        // Create and save the new meeting
        log.info("User {} starting new meeting «{}»", userId, req.getTitle());
        Meeting meeting = Meeting.builder()
                .title(req.getTitle())
                .hostUserId(userId)
                .createdAt(Instant.now())
                .scheduledFor(req.getScheduledFor() != null ? req.getScheduledFor() : Instant.now()) // Default to now if not scheduled
                .build();
        repo.save(meeting);

        // Map the meeting to a response
        return map(meeting);
    }

    /**
     * Schedules a meeting for a specific time.
     *
     * @param req    the request containing meeting details.
     * @param userId the ID of the user scheduling the meeting.
     * @return a response containing meeting details.
     */
    @Override
    public MeetingResponse schedule(CreateMeetingRequest req, String userId) {
        // Validate request
        if (req.getScheduledFor() == null || req.getScheduledFor().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }

        // Create and save the scheduled meeting
        log.info("User {} scheduling meeting «{}» for {}", userId, req.getTitle(), req.getScheduledFor());
        Meeting meeting = Meeting.builder()
                .title(req.getTitle())
                .hostUserId(userId)
                .scheduledFor(req.getScheduledFor())
                .createdAt(Instant.now())
                .build();
        repo.save(meeting);

        // Map the meeting to a response
        return map(meeting);
    }

    /**
     * Joins an existing meeting.
     *
     * @param req the request containing the meeting ID.
     * @return a response containing meeting details.
     */
    @Override
    public MeetingResponse join(JoinMeetingRequest req) {
        // Find the meeting by ID
        Meeting m = repo.findById(req.getMeetingId())
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found"));
        log.info("Joining meeting {}", m.getId());

        // Check if the meeting is scheduled for the future
        if (m.getScheduledFor() != null && m.getScheduledFor().isAfter(Instant.now())) {
            throw new IllegalStateException("Cannot join a meeting that is scheduled for the future");
        }

        // Map the meeting to a response
        return map(m);
    }

    /**
     * Ends an ongoing meeting.
     *
     * @param id          the ID of the meeting to end.
     * @param callerUserId the ID of the user attempting to end the meeting.
     * @return a response containing the updated meeting details.
     */
    public MeetingResponse endMeeting(Long id, String callerUserId) {
        Meeting m = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        // Optional: verify the caller is the host
        if (!Objects.equals(m.getHostUserId(), callerUserId)) {
            throw new AccessDeniedException("Only the host can end the meeting");
        }
        if (m.isEnded()) {                               // already ended → idempotent
            return map(m);
        }

        m.setEnded(true);
        // If you adopt endedAt instead of a boolean:
        // m.setEndedAt(Instant.now());

        return map(repo.save(m));  // save and return the updated meeting
    }

    private MeetingResponse map(Meeting m) {
        String joinUrl = "/join/" + m.getId();      // front‑end expands this
        return MeetingResponse.builder()
                .id(m.getId())
                .title(m.getTitle())
                .scheduledFor(m.getScheduledFor())
                .joinUrl(joinUrl)
                .build();
    }
}

