package startup.backend.service;

import java.util.List;

public interface LobbyService {
    void addToLobby(String meetingId, Long userId);

    void approveEntry(String meetingId, Long userId);

    List<Long> getLobbyUsers(String meetingId);
}
