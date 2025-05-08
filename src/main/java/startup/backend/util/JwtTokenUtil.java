package startup.backend.util;

import jakarta.servlet.http.HttpServletRequest;
import startup.backend.entity.Role;
import startup.backend.entity.User;
import startup.backend.exception.JwtTokenException;
import startup.backend.exception.JwtTokenExpiredException;
import startup.backend.exception.JwtTokenParseException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import startup.backend.repository.UserRepository;

import javax.crypto.SecretKey;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    private SecretKey secretKey;

    @Value("${jwt.secretKey}")
    private String secretKeyString;

    @Value("${jwt.expirationMs}")
    private Long expirationMs;

    private final UserRepository userRepository;

    public JwtTokenUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @PostConstruct
    public void init() {
        if (Objects.isNull(expirationMs) || expirationMs <= 0) {
            expirationMs = 3600000L;

        }
        try {
            this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Error initializing JwtTokenUtil with the secret key: {}", e.getMessage(), e);
            this.secretKey = Keys.hmacShaKeyFor("defaultSecretKey".getBytes(StandardCharsets.UTF_8));
        }
    }


    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtTokenParseException e) {
            logError("Failed to extract username from the token", e);
            throw new JwtTokenException("Failed to extract username from the token: " + e.getMessage(), e);
        }
    }

    public Long getUserIdFromToken(String token) {
        return extractAllClaims(token).get("id", Long.class); // ✅ Extract userId claim
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (JwtTokenParseException e) {
            logError("Failed to extract expiration from the token", e);
            throw new JwtTokenException("Failed to extract expiration from the token: " + e.getMessage(), e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public Claims extractAllClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            if (claims.getExpiration().before(new Date())) {
                throw new JwtTokenExpiredException("JWT token has expired.");
            }
            return claims;
        } catch (JwtException e) {
            throw new JwtTokenExpiredException("JWT token has expired: " + e.getMessage());
        }
    }

    private static final long ALLOWED_CLOCK_SKEW = 5 * 60 * 1000;
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            long currentTime = System.currentTimeMillis();
            long tokenExpirationTime = expiration.getTime();
            return tokenExpirationTime < (currentTime - ALLOWED_CLOCK_SKEW);
        } catch (JwtTokenException e) {
            logError("Error while checking token expiration", e);
            return true;
        }
    }



    public String generateToken(String username, Integer userId, Set<Role> roles) {
        String[] roleNames = roles.stream()
                .map(role -> role.getName().name())
                .toArray(String[]::new);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roleNames)
                .claim("id", userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            if (extractedUsername.equals(username) && !isTokenExpired(token)) {
                logger.info("Token validation successful for user: {}", username);
                return true;
            } else {
                logger.warn("Token validation failed for user {}: Token expired or invalid username", username);
                return false;
            }
        } catch (JwtTokenException e) {
            logError("Token validation failed", e);
            return false;
        }
    }


    public Claims extractResetPasswordClaims(String token) {
        try {
            return extractAllClaims(token);
        } catch (JwtTokenParseException e) {
            logError("Failed to extract claims from reset password token", e);
            return Jwts.claims(Collections.emptyMap());
        }
    }

    private void logError(String message, Exception e) {
        logger.error("{}: {}", message, e.getMessage(), e);
    }

    // Method to get the current user based on JWT token
    public User getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);  // Extract token from the request header
        if (token == null || isTokenExpired(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        Long userId = getUserIdFromToken(token);  // Extract user ID from token
        if (userId == null) {
            throw new RuntimeException("User ID is missing in the token");
        }
        return userRepository.findById(userId)  // Retrieve user by ID
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Helper method to extract token from the Authorization header
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);  // Extract the token (after "Bearer ")
        }
        return null;

    }
}
