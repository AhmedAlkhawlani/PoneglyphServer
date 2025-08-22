package com.nova.poneglyph.domain.moderation;

import com.nova.poneglyph.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_blocks",
        indexes = {
                @Index(name = "idx_blocks_blocker", columnList = "blocker_id"),
                @Index(name = "idx_blocks_blocked", columnList = "blocked_id")
        })
@IdClass(UserBlock.PK.class)
public class UserBlock {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(name = "block_reason", length = 255)
    private String blockReason;

    @Column(name = "is_silent")
    private boolean silent;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private UUID blocker;
        private UUID blocked;
    }
}
