package edutech.backend.util;

import edutech.backend.entity.Role;
import edutech.backend.exception.JwtTokenException;
import edutech.backend.exception.JwtTokenExpiredException;
import edutech.backend.exception.JwtTokenParseException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
}
