package com.nova.poneglyph.events;

import lombok.Data;

import java.util.UUID;

@Data
public class PresenceMessage {
    private UUID userId;
    private boolean online;
    private String status;
    private long timestamp;
}
