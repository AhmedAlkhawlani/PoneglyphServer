//package com.nova.poneglyph.domain.user;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.OffsetDateTime;
//import java.util.UUID;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@Entity
//@Table(name = "user_sessions",
//        indexes = {
//                @Index(name = "idx_sessions_user", columnList = "user_id"),
//                @Index(name = "idx_sessions_device", columnList = "device_uuid")
//        })
//public class UserSession {
//
//    @Id
//    @Column(name = "session_uuid", nullable = false, updatable = false)
//    private UUID id;
//
//    @PrePersist
//    public void prePersist() {
//        if (id == null) id = UUID.randomUUID();
//    }
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "device_uuid")
//    private UserDevice device;
//
//    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 128)
//    private String refreshTokenHash;
//
//    @Column(name = "issued_at")
//    private OffsetDateTime issuedAt;
//
//    @Column(name = "last_used_at")
//    private OffsetDateTime lastUsedAt;
//
//    @Column(name = "expires_at", nullable = false)
//    private OffsetDateTime expiresAt;
//
//    @Column(name = "revoked_at")
//    private OffsetDateTime revokedAt;
//}
package com.nova.poneglyph.domain.user;



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
@Table(name = "user_sessions",
        indexes = {
                @Index(name = "idx_sessions_user", columnList = "user_id"),
                @Index(name = "idx_sessions_device", columnList = "device_uuid"),
                @Index(name = "idx_sessions_refresh_hash", columnList = "refresh_token_hash"),
                @Index(name = "idx_sessions_expires", columnList = "expires_at")
        })
public class UserSession {

    @Id
    @Column(name = "session_uuid", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_uuid")
    private UserDevice device; // استخدم العلاقة بدلاً من تخزين deviceId منفصل
    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 128)
    private String refreshTokenHash;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "replaced_by")
    private UUID replacedBy;

    // الحقول الجديدة المطلوبة
    @Column(name = "online", nullable = false)
    private boolean online = false;

    @Column(name = "websocket_session_id", length = 255)
    private String websocketSessionId;

    @Column(name = "last_activity")
    private OffsetDateTime lastActivity;

    // إزالة الحقل device_id المكرر
    // @Column(name = "device_id", length = 255)
    // private String deviceId;

    @Column(name = "active_jti")
    private UUID activeJti;

    @Version
    @Column(name = "version")
    private Long version;

    // دالة مساعدة للحصول على deviceId من العلاقة
    public String getDeviceId() {
        return this.device != null ? this.device.getDeviceId() : null;
    }
    // دوال مساعدة
    public boolean isValid() {
        return active && revokedAt == null && expiresAt.isAfter(OffsetDateTime.now());
    }

    public boolean isExpired() {
        return expiresAt.isBefore(OffsetDateTime.now());
    }

    public void revoke() {
        this.revokedAt = OffsetDateTime.now();
        this.active = false;
    }

    public void updateLastActivity() {
        this.lastActivity = OffsetDateTime.now();
        this.lastUsedAt = OffsetDateTime.now();
    }
}
