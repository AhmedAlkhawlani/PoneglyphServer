package com.nova.poneglyph.dto.conversation;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ParticipantDTO {
    private UUID userId;
    private String phoneNumber;
    private String displayName;
    private String role;
    private OffsetDateTime joinedAt;
    private OffsetDateTime leftAt;
    private Integer unreadCount;
}
