package com.nova.poneglyph.domain.auth;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "jwt_keys")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JwtKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Key identifier exposed in JWT header + JWKS */
    @Column(name = "kid", nullable = false, unique = true, length = 64)
    private String kid;

    /** CURRENT or ARCHIVED */
    @Column(name = "key_type", nullable = false, length = 16)
    private String keyType;

    /** Algorithm, e.g., RS256 */
    @Column(name = "alg", nullable = false, length = 16)
    private String alg;

//    /** Base64-encoded PKCS#8 private key, optionally KMS-encrypted. */
//    @Lob
//    @Column(name = "private_key_pem", nullable = false)
//    private String privateKeyPem; // store ciphertext if KMS is used
//
//    /** Base64-encoded X.509 SubjectPublicKeyInfo (PEM w/o headers) */
//    @Lob
//    @Column(name = "public_key_pem", nullable = false)
//    private String publicKeyPem;

    /** استبدال @Lob بـ columnDefinition = "text" */
    @Column(name = "private_key_pem", nullable = false, columnDefinition = "text")
    private String privateKeyPem;

    @Column(name = "public_key_pem", nullable = false, columnDefinition = "text")
    private String publicKeyPem;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt; // only for ARCHIVED

    public static JwtKey createCurrent(String kid, String alg, String privPem, String pubPem) {
        return JwtKey.builder()
                .kid(kid)
                .keyType("CURRENT")
                .alg(alg)
                .privateKeyPem(privPem)
                .publicKeyPem(pubPem)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    public static JwtKey createArchived(String kid, String alg, String privPem, String pubPem) {
        return JwtKey.builder()
                .kid(kid)
                .keyType("ARCHIVED")
                .alg(alg)
                .privateKeyPem(privPem)
                .publicKeyPem(pubPem)
                .createdAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusDays(30))
                .build();
    }
}
