package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import startup.backend.entity.Meeting;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long>
{
    Optional<Meeting> findByMeetingId(String meetingId);
    List<Meeting> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}
