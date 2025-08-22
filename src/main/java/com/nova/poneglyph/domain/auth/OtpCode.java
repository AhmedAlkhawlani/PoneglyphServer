package com.nova.poneglyph.domain.auth;



import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "otp_codes")
public class OtpCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "normalized_phone", nullable = false, length = 20)
    private String normalizedPhone;

    @Column(name = "code_hash", nullable = false, length = 128)
    private String codeHash;

    @Column(name = "method", length = 20)
    private String method = "sms";

    @Column(name = "created_at", nullable = false)

    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "requester_ip", length = 45)
    private String requesterIp;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;


}
