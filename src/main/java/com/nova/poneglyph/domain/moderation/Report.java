package com.nova.poneglyph.domain.moderation;

import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.enums.ReportStatus;
import com.nova.poneglyph.domain.message.Message;
import com.nova.poneglyph.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reports",
        indexes = {
                @Index(name = "idx_reports_reporter", columnList = "reporter_id"),
                @Index(name = "idx_reports_reported", columnList = "reported_user_id"),
                @Index(name = "idx_reports_status", columnList = "status")
        })
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @Column(name = "reported_phone", length = 20)
    private String reportedPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(name = "report_type", nullable = false, length = 32)
    private String reportType; // spam|abuse|...

    @Column(name = "report_details")
    private String reportDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "admin_notes")
    private String adminNotes;

    @Column(name = "created_at")
    private java.time.OffsetDateTime createdAt;

    @Column(name = "resolved_at")
    private java.time.OffsetDateTime resolvedAt;
}
