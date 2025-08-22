package com.nova.poneglyph.dto.chatDto;

import com.nova.poneglyph.domain.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Data
public class MessageDto {
    private UUID messageId;
    private UUID conversationId;
    private UUID senderId;
    private MessageType messageType;
    private String content;
    private String contentHash;
    private UUID replyToId;
    private java.time.OffsetDateTime sentAt;
    private List<MediaDto> mediaAttachments;
}
