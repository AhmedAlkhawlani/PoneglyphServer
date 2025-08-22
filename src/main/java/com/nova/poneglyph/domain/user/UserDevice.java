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
@Table(name = "user_devices",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_device", columnNames = {"user_id", "device_id"}),
        indexes = @Index(name = "idx_user_devices_user", columnList = "user_id"))
public class UserDevice extends Auditable {

    @Id
    @Column(name = "device_uuid", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_id", nullable = false, length = 255)
    private String deviceId; // client-provided unique ID

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "ip_address")
    private String ipAddress; // store as text; or map to inet via custom type

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "is_active")
    private boolean active = true;
}
