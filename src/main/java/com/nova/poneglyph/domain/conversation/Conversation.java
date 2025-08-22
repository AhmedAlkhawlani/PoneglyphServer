package com.nova.poneglyph.domain.conversation;

import com.nova.poneglyph.domain.base.Auditable;
import com.nova.poneglyph.domain.enums.ConversationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conversations",
        indexes = @Index(name = "idx_conv_last_msg", columnList = "last_message_at"))
public class Conversation extends Auditable {

    @Id
    @Column(name = "conversation_id", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type", nullable = false, length = 16)
    private ConversationType type = ConversationType.DIRECT;

    @Column(name = "is_encrypted")
    private boolean encrypted = true;

    @Column(name = "encryption_key")
    private String encryptionKey;

    @Column(name = "last_message_at")
    private OffsetDateTime lastMessageAt;
}
