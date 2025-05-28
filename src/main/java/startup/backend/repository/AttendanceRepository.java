package startup.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import startup.backend.entity.Attendance;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance,Long> {
    Optional<Attendance> findByUserIdAndDate(String userId, String date);
}
