package com.nova.poneglyph.domain.audit;

import com.nova.poneglyph.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_log",
        indexes = @Index(name = "idx_audit_target", columnList = "target_type, target_id"))
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "target_type", nullable = false, length = 32)
    private String targetType; // user|conversation|message|...

    @Column(name = "target_id", nullable = false, length = 64)
    private String targetId; // flexible

    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;

    @Column(name = "created_at")
    private java.time.OffsetDateTime createdAt;
}
