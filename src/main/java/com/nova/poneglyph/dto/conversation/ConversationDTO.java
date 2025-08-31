// ConversationDTO.java
package com.nova.poneglyph.dto.conversation;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ConversationDTO {
    private UUID id;
    private String type;
    private boolean encrypted;
    private OffsetDateTime lastMessageAt;
    private List<ParticipantDTO> participants;
    private MessageDTO lastMessage;
    // اسم العرض: للطرف الآخر لدى DIRECT، وللمجموعة نستخدم title
    private String displayName;

    // عدد المشتركين (مفيد للمجموعات)
    private Integer participantCount;
    private Integer unreadCount;
}


