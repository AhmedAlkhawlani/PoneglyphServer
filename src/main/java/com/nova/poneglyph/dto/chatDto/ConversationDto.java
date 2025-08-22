package com.nova.poneglyph.dto.chatDto;

import com.nova.poneglyph.domain.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ConversationDto {
    private UUID conversationId;
    private ConversationType type;
    private String encryptionKey;
    private java.time.OffsetDateTime lastMessageAt;
}
