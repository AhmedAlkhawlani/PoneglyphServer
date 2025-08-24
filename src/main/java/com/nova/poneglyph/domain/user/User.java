

package com.nova.poneglyph.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.poneglyph.domain.base.Auditable;
import com.nova.poneglyph.domain.enums.AccountStatus;
import com.nova.poneglyph.dto.NotificationSettings;
import com.nova.poneglyph.dto.PrivacySettings;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_norm_phone", columnList = "normalized_phone"),
                @Index(name = "idx_users_status", columnList = "account_status")
        })
public class User extends Auditable {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber; // E.164 (+966...)

    @Column(name = "country_code", nullable = false, length = 5)
    private String countryCode;

    @Column(name = "normalized_phone", nullable = false, unique = true, length = 20)
    private String normalizedPhone; // generated column in DB

    @Column(name = "is_verified")
    private boolean verified;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 16)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "ban_reason")
    private String banReason;

    @Column(name = "ban_expiry")
    private OffsetDateTime banExpiry;

    @Column(name = "encrypted_phone", length = 128)
    private String encryptedPhone;

    @Column(name = "last_active")
    private OffsetDateTime lastActive;

    // الحقول الجديدة المطلوبة
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "login_count", nullable = false)
    private int loginCount = 0;

    @Column(name = "privacy_settings", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String privacySettings;

    @Column(name = "notification_settings", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String notificationSettings;

    @Column(name = "encrypted_private_key", length = 512)
    private String encryptedPrivateKey;

    @Column(name = "public_key", length = 512)
    private String publicKey;

    @Column(name = "device_count", nullable = false)
    private int deviceCount = 0;

    @Column(name = "is_online")
    private boolean online = false;

    @Column(name = "websocket_session_id")
    private String websocketSessionId;

    @Version
    @Column(name = "version")
    private Long version;

    public String getDisplayName() {
        return phoneNumber; // أو حط عندك username لو موجود
    }

    public PrivacySettings getPrivacySettingsObject() {
        if (privacySettings == null) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(privacySettings, PrivacySettings.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse privacy_settings", e);
        }
    }

    public void setPrivacySettingsObject(PrivacySettings settings) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.privacySettings = mapper.writeValueAsString(settings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize privacy_settings", e);
        }
    }


    // تحويل JSON إلى كائن
    public NotificationSettings getNotificationSettingsObject() {
        if (notificationSettings == null) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(notificationSettings, NotificationSettings.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse notification_settings", e);
        }
    }

    // تحديث الإعدادات
    public void setNotificationSettingsObject(NotificationSettings settings) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.notificationSettings = mapper.writeValueAsString(settings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize notification_settings", e);
        }
    }
}
