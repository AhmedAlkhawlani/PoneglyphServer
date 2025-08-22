package com.nova.poneglyph.domain.message;

import com.nova.poneglyph.domain.enums.DeliveryStatus;
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
@Table(name = "message_status",
        indexes = @Index(name = "idx_msgstatus_user", columnList = "user_id, status"))
@IdClass(MessageStatus.PK.class)
public class MessageStatus {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private DeliveryStatus status;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private UUID message;
        private UUID user;
    }

}
