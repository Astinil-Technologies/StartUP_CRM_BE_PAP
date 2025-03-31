package startup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import startup.backend.entity.CheckOut;

import java.util.Optional;

public interface CheckOutRepository extends JpaRepository<CheckOut, Long> {
    Optional<CheckOut> findTopByUserIdOrderByCheckOutTimeDesc(String userId);
}
