package com.nova.poneglyph.domain.conversation;

import com.nova.poneglyph.domain.enums.ParticipantRole;
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
@Table(name = "participants",
        uniqueConstraints = @UniqueConstraint(name = "uk_conv_user", columnNames = {"conversation_id", "user_id"}),
        indexes = {
                @Index(name = "idx_part_user", columnList = "user_id"),
                @Index(name = "idx_part_conv", columnList = "conversation_id")
        })
public class Participant {

    @Id
    @Column(name = "participant_id", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 16)
    private ParticipantRole role = ParticipantRole.MEMBER;

    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;

    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    @Column(name = "mute_until")
    private OffsetDateTime muteUntil;

    @Column(name = "unread_count")
    private Integer unreadCount = 0;

    @Column(name = "last_read_seq")
    private Long lastReadSeq = 0L;
}
