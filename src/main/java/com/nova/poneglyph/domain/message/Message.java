package com.nova.poneglyph.domain.message;

import com.nova.poneglyph.domain.base.Auditable;
import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.enums.MessageType;
import com.nova.poneglyph.domain.user.User;
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
@Table(name = "messages",
        indexes = {
                @Index(name = "idx_msgs_conv_seq", columnList = "conversation_id, created_at, sequence_number"),
                @Index(name = "idx_msgs_sender", columnList = "sender_id, created_at")
        })
public class Message extends Auditable {

    @Id
    @Column(name = "message_id", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 16)
    private MessageType messageType;

    @Lob
    @Column(name = "encrypted_content", nullable = false)
    private byte[] encryptedContent;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @Column(name = "sequence_number", insertable = false, updatable = false)
    private Long sequenceNumber;

    @Column(name = "deleted_for_all")
    private boolean deletedForAll;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Media> mediaAttachments = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<MessageStatus> statuses = new java.util.HashSet<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.Set<MessageReaction> reactions = new java.util.HashSet<>();

}
