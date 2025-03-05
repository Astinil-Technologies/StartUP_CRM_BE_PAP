package edutech.backend.service;

import edutech.backend.config.JwtConfig;
import edutech.backend.entity.RefreshToken;
import edutech.backend.entity.User;
import edutech.backend.exception.CustomException;
import edutech.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;
    /**
     * Checks if a refresh token is revoked.
     *
     * @param token the RefreshToken object.
     * @return true if the token is revoked, else false.
     */
    public boolean isRevoked(RefreshToken token) {
        return token.isRevoked();
    }

    /**
     * Finds a refresh token by its string value.
     *
     * @param token the string value of the refresh token.
     * @return An Optional containing the RefreshToken if found.
     */
    public Optional<RefreshToken> findByToken(String token) {
        log.info("Fetching refresh token for token value: {}", token);
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Creates a new refresh token for the user or updates the existing one.
     *
     * @param user the User for whom the token is being created.
     * @return The created or updated RefreshToken.
     */
    public RefreshToken createRefreshToken(User user) {

        Optional<RefreshToken> existingTokenOptional = refreshTokenRepository.findByUser(user);

        if (existingTokenOptional.isPresent()) {

            RefreshToken existingToken = existingTokenOptional.get();
            existingToken.setToken(UUID.randomUUID().toString());
            existingToken.setExpiryDate(Instant.now().plusMillis(jwtConfig.getRefreshExpirationMs()));
            existingToken.setRevoked(false);
            refreshTokenRepository.save(existingToken); // Save updated token
            log.info("Updated refresh token for user: {}", user.getUsername());
            return existingToken;
        }


        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(user);
        newRefreshToken.setToken(UUID.randomUUID().toString());
        newRefreshToken.setExpiryDate(Instant.now().plusMillis(jwtConfig.getRefreshExpirationMs()));
        newRefreshToken.setRevoked(false);
        refreshTokenRepository.save(newRefreshToken);
        log.info("Created a new refresh token for user: {}", user.getUsername());
        return newRefreshToken;
    }

    /**
     * Checks if a refresh token has expired.
     *
     * @param token the RefreshToken object.
     * @return true if the token is expired, else false.
     */
    public boolean isExpired(RefreshToken token) {
        boolean expired = token.getExpiryDate().isBefore(Instant.now());
        if (expired) {
            log.info("Refresh token has expired: {}", token.getToken());
        }
        return expired;
    }

    /**
     * Revokes a refresh token by setting its revoked property to true.
     *
     * @param token the string value of the refresh token to revoke.
     */
    public void revokeToken(String token) {
        Optional<RefreshToken> refreshTokenOptional = findByToken(token);

        if (refreshTokenOptional.isEmpty()) {
            log.error("Invalid refresh token: {}", token);
            throw new CustomException("Invalid refresh token provided.");
        }

        RefreshToken refreshToken = refreshTokenOptional.get();
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token '{}' has been revoked.", token);
    }
}