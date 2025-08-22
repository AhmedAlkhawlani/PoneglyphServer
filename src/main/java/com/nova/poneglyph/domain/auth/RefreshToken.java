package com.nova.poneglyph.domain.auth;



import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;
import com.nova.poneglyph.domain.user.UserSession;
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
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @Column(name = "jti", nullable = false)
    private UUID jti = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_hash", nullable = false, unique = true, length = 128)
    private String refreshHash;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false ) // جعله غير nullable
    private UserSession session;
    // إزالة الحقل device_id المكرر
    // @Column(name = "device_id", length = 255)
    // private String deviceId;
    // استخدام علاقة مباشرة مع UserDevice بدلاً من deviceId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_uuid")
    private UserDevice device;
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt = OffsetDateTime.now();

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "replaced_by")
    private UUID replacedBy;

    // دالة مساعدة للحصول على deviceId من الجلسة
    public String getDeviceId() {
        return this.session != null ? this.session.getDeviceId() : null;
    }
    // في كيان RefreshToken
//    public void setRevokedAt(OffsetDateTime revokedAt) {
//        this.revokedAt = revokedAt;
//    }
}
