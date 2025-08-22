package com.nova.poneglyph.domain.message;

import com.nova.poneglyph.domain.conversation.Conversation;
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
@Table(name = "calls",
        indexes = {
                @Index(name = "idx_calls_caller", columnList = "caller_id"),
                @Index(name = "idx_calls_receiver", columnList = "receiver_id")
        })
public class Call {

    @Id
    @Column(name = "call_id", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id")
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Column(name = "call_type", nullable = false, length = 10)
    private String callType; // audio|video

    @Column(name = "status", nullable = false, length = 15)
    private String status; // initiated|ongoing|completed|missed|rejected

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "encryption_key")
    private String encryptionKey;

    @Column(name = "is_recorded")
    private boolean recorded;
}
