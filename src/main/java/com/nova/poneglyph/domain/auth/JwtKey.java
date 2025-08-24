package com.nova.poneglyph.domain.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "jwt_keys")
@Getter
@Setter
@NoArgsConstructor
public class JwtKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_type", nullable = false) // CURRENT أو ARCHIVED
    private String keyType;

    @Column(name = "secret", nullable = false, length = 512)
    private String secret;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    // إنشاء كائن JwtKey بطريقة أكثر أماناً
    public static JwtKey createCurrentKey(String secret) {
        JwtKey key = new JwtKey();
        key.setKeyType("CURRENT");
        key.setSecret(secret);
        key.setCreatedAt(OffsetDateTime.now());
        return key;
    }

    public static JwtKey createArchivedKey(String secret) {
        JwtKey key = new JwtKey();
        key.setKeyType("ARCHIVED");
        key.setSecret(secret);
        key.setCreatedAt(OffsetDateTime.now());
        key.setExpiresAt(OffsetDateTime.now().plusDays(30));
        return key;
    }
}
