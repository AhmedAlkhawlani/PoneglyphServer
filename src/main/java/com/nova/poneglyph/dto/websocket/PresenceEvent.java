package com.nova.poneglyph.dto.websocket;

import lombok.Data;
import java.util.UUID;

@Data
public class PresenceEvent {
    private UUID userId;
    private boolean online;
    private Long timestamp;
    private String status; // online, offline, away, busy
}
