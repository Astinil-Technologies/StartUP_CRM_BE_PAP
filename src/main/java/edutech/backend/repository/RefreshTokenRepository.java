package edutech.backend.repository;

import edutech.backend.entity.RefreshToken;
import edutech.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
