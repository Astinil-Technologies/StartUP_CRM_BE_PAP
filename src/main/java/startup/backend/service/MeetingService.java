package startup.backend.service;

import startup.backend.dto.CreateMeetingRequest;
import startup.backend.dto.JoinMeetingRequest;
import startup.backend.dto.MeetingResponse;


public interface MeetingService {
    MeetingResponse start(CreateMeetingRequest req, String userId);

    MeetingResponse schedule(CreateMeetingRequest req, String userId);

    MeetingResponse join(JoinMeetingRequest req);

    MeetingResponse endMeeting(Long id, String userId);
}
