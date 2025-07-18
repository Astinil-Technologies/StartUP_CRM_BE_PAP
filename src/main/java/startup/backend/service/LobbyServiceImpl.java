package startup.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class LobbyServiceImpl implements LobbyService{


    // In-memory map to store meetingId -> Set of userIds in lobby
    private final Map<String, Set<Long>> lobbyMap = new HashMap<>();


    private final MeetingService meetingService;


    @Override
    public void addToLobby(String meetingId, Long userId) {
        // Add userId to the lobby for the given meetingId
        lobbyMap.computeIfAbsent(meetingId, key -> new HashSet<>()).add(userId);

    }

    @Override
    public void approveEntry(String meetingId, Long userId) {
        // Remove user from lobby and (optionally) join them to the meeting
        Set<Long> users = lobbyMap.get(meetingId);
        if (users != null && users.remove(userId)) {
            meetingService.joinMeeting(meetingId, userId);
            System.out.println("User " + userId + " approved and added to meeting " + meetingId);
        } else {
            throw new RuntimeException("User not in lobby or already approved.");
        }
    }

    @Override
    public List<Long> getLobbyUsers(String meetingId) {
        // Return list of users in the lobby
        Set<Long> users = lobbyMap.get(meetingId);
        return users == null ? List.of() : new ArrayList<>(users);
    }
}
