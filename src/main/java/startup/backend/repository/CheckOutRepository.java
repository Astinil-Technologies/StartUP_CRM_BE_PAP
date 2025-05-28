package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import startup.backend.entity.CheckOut;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckOutRepository extends JpaRepository<CheckOut, Long> {
    Optional<CheckOut> findTopByUserIdOrderByCheckOutTimeDesc(String userId);

    Optional<CheckOut> findTopByUserIdOrderByCheckInTimeDesc(String userId);

    List<CheckOut> findByUserIdAndCheckInTimeBetween(String userId, LocalDateTime checkInTime, LocalDateTime checkOutTime);
}
