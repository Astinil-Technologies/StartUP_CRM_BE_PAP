package startup.backend.dto;

import lombok.*;

import java.time.Instant;

@Builder
public record ShareScreenTokenResponse(
        String token,
        Instant expiresAt
) {}
