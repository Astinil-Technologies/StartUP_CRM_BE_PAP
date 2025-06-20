package startup.backend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class ShareScreenTokenService {

    private final Key key;
    private final long ttlSeconds;

    public ShareScreenTokenService(
            @Value("${security.screenshare.secret}") String secret,
            @Value("${security.screenshare.ttl:300}") long ttlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.ttlSeconds = ttlSeconds;
    }

    public String generate(Long meetingId, String userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);
        return Jwts.builder()
                .setIssuer("meeting‑svc")
                .setSubject(userId)
                .claim("meetingId", meetingId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key)
                .compact();
    }
}

