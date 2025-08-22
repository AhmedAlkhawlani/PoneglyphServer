package com.nova.poneglyph.domain.user;

import com.nova.poneglyph.domain.base.Auditable;
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
@Table(name = "user_profiles")
public class UserProfile extends Auditable {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId; // 1:1 with users

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "about_text", length = 140)
    private String aboutText;

    @Column(name = "status_emoji", length = 2)
    private String statusEmoji;

    @Column(name = "last_profile_update")
    private OffsetDateTime lastProfileUpdate;
}
