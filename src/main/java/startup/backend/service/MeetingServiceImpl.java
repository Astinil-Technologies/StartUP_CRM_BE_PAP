package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import startup.backend.dto.MeetingDto;
import startup.backend.entity.Meeting;
import startup.backend.entity.User;
import startup.backend.exception.CustomException;
import startup.backend.repository.MeetingRepository;
import startup.backend.repository.UserRepository;
import startup.backend.service.MeetingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Override
    public MeetingDto createMeeting(MeetingDto dto) {
        User host = userRepository.findById(dto.getHostId())
                .orElseThrow(() -> new CustomException("Host not found"));

        Meeting meeting = Meeting.builder()
                .title(dto.getTitle())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .host(host)
                .lobbyEnabled(dto.isLobbyEnabled())
                .build();

        return toDto(meetingRepository.save(meeting));
    }

    @Override
    public MeetingDto joinMeeting(String meetingId, Long userId) {
        Meeting meeting = meetingRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new CustomException("Meeting not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));

        meeting.getParticipants().add(user);
        return toDto(meetingRepository.save(meeting));
    }

    @Override
    public List<MeetingDto> getUpcomingMeetings(Long userId) {
        return meetingRepository.findAll().stream()
                .filter(m -> m.getStartTime().isAfter(LocalDateTime.now()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @  Override
    public List<MeetingDto> getPastMeetings(Long userId) {
        return meetingRepository.findAll().stream()
              .filter(m -> m.getEndTime().isBefore(LocalDateTime.now()))
                .filter(m -> m.getParticipants().stream().anyMatch(u -> u.getId().equals(userId)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MeetingDto updateMeeting(Long id, MeetingDto dto) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new CustomException("Meeting not found"));

        meeting.setTitle(dto.getTitle());
        meeting.setStartTime(dto.getStartTime());
        meeting.setEndTime(dto.getEndTime());
        meeting.setLocked(dto.isLocked());
        meeting.setLobbyEnabled(dto.isLobbyEnabled());

        return toDto(meetingRepository.save(meeting));
    }

    @Override
    public void deleteMeeting(Long id) {
        if (!meetingRepository.existsById(id)) {
            throw new CustomException("Meeting not found");
        }
        meetingRepository.deleteById(id);
    }

    @Override
    public void lockMeeting(String meetingId) {
        Meeting meeting = meetingRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new CustomException("Meeting not found"));
        meeting.setLocked(true);
        meetingRepository.save(meeting);
    }

    @Override
    public void kickUser(String meetingId, Long userId) {
        Meeting meeting = meetingRepository.findByMeetingId(meetingId)
                .orElseThrow(() -> new CustomException("Meeting not found"));

        meeting.getParticipants().removeIf(user -> user.getId().equals(userId));
        meetingRepository.save(meeting);
    }

    private MeetingDto toDto(Meeting meeting) {
        Set<Long> participantIds = meeting.getParticipants() != null ?
                meeting.getParticipants().stream().map(User::getId).collect(Collectors.toSet()) : Set.of();

        return MeetingDto.builder()
                .id(meeting.getId())
                .meetingId(meeting.getMeetingId())
                .title(meeting.getTitle())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .hostId(meeting.getHost().getId())
                .participantIds(participantIds)
                .locked(meeting.isLocked())
                .started(meeting.isStarted())
                .lobbyEnabled(meeting.isLobbyEnabled())
                .build();
    }
}
