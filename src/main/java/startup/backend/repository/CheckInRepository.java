package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import startup.backend.entity.CheckIn;

import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    Optional<CheckIn> findTopByUserIdOrderByCheckInTimeDesc(String userId);
}