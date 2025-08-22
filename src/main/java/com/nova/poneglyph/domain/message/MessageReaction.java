package com.nova.poneglyph.domain.message;

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
@Table(name = "message_reactions")
@IdClass(MessageReaction.PK.class)
public class MessageReaction {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "reaction", nullable = false, length = 20)
    private String reaction;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private UUID message;
        private UUID user;
    }

}
