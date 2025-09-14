package com.nova.poneglyph.domain.user;

import com.nova.poneglyph.domain.enums.SyncStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contacts",
        indexes = @Index(name = "idx_contacts_user", columnList = "user_id"))
@IdClass(Contact.PK.class)
public class Contact {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @Column(name = "normalized_phone", nullable = false, length = 20) // أزل insertable/updatable false
    private String normalizedPhone;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone; // E.164

    @Column(name = "contact_name", length = 100)
    private String contactName;
    @Column(name = "registered_id")
    private String registeredId;

    @Column(name = "is_registered")
    private boolean registered;

    @Column(name = "last_seen")
    private java.time.OffsetDateTime lastSeen;

    @Column(name = "is_blocked")
    private boolean blocked;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", length = 16)
    private SyncStatus syncStatus = SyncStatus.NEW;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private java.util.UUID user;
        private String normalizedPhone;
    }
}
