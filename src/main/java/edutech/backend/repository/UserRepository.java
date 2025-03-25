package edutech.backend.repository;

import edutech.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByMobileNo(String mobile_no);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNo(String mobile_no);
}