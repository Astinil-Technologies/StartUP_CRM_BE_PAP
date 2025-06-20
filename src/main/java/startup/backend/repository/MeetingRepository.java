package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import startup.backend.entity.Meeting;

import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // Checks if the user is currently hosting any active (not ended) meeting
    boolean existsByHostUserIdAndEndedFalse(String userId);

    // Optional: find a meeting by ID and host (useful for access check)
    Optional<Meeting> findByIdAndHostUserId(Long id, String hostUserId);
}


