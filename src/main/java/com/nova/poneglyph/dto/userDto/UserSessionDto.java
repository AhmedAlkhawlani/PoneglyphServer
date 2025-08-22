package com.nova.poneglyph.dto.userDto;

// dto/UserSessionDto.java

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserSessionDto(
        UUID id,
        String deviceId,
        String deviceName,
        OffsetDateTime issuedAt,
        OffsetDateTime lastUsedAt,
        OffsetDateTime expiresAt,
        boolean active,
        String ipAddress,
        String userAgent,
        boolean online,
        UUID activeJti
) {}
