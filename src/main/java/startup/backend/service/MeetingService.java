package startup.backend.service;

import startup.backend.dto.MeetingDto;

import java.util.List;

public interface MeetingService {
    MeetingDto createMeeting(MeetingDto dto);

    MeetingDto joinMeeting(String meetingId, Long userId);

    List<MeetingDto> getUpcomingMeetings(Long userId);

    List<MeetingDto> getPastMeetings(Long userId);

    MeetingDto updateMeeting(Long id, MeetingDto dto);

    void deleteMeeting(Long id);

    void lockMeeting(String meetingId);

    void kickUser(String meetingId, Long userId);
}
