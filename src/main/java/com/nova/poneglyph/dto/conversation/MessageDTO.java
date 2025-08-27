package com.nova.poneglyph.dto.conversation;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MessageDTO {
    private UUID id;
    private UUID conversationId;
    private UUID senderId;
    private String senderPhone;
    private String senderName;
    private String messageType;
    private String content;
    private OffsetDateTime createdAt;
    private Long sequenceNumber;
    private List<MediaDTO> mediaAttachments;
    private MessageDTO replyTo;
    private String status;
}
