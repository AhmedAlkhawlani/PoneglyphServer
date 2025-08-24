Below are **production-ready files** to make your server's JWT/key layer stronger and more resilient:

- RS256 with **`kid`** + **JWKS endpoint**
- Thread-safe key handling + **in-memory & Redis caching**
- **Archived keys** validation (old tokens keep working)
- **KMS-ready** (pluggable provider) with local/noop fallback
- **Persistence across restarts** (DB + Redis) and cleanup jobs
- Grace handling for **expired-but-signed** tokens in controlled paths (e.g., refresh)

> If you keep your current HMAC setup, switch to this RSA-based one; it’s more flexible for multi-service verification and safer distribution of public keys.

---

## 1) `JwtKey.java` (Entity) — upgraded for RSA & JWKS
```java
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

    /** Base64-encoded PKCS#8 private key, optionally KMS-encrypted. */
    @Lob
    @Column(name = "private_key_pem", nullable = false)
    private String privateKeyPem; // store ciphertext if KMS is used

    /** Base64-encoded X.509 SubjectPublicKeyInfo (PEM w/o headers) */
    @Lob
    @Column(name = "public_key_pem", nullable = false)
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
```

---

## 2) `JwtKeyRepository.java`
```java
package com.nova.poneglyph.repository;

import com.nova.poneglyph.domain.auth.JwtKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JwtKeyRepository extends JpaRepository<JwtKey, Long> {
    Optional<JwtKey> findTopByKeyTypeOrderByCreatedAtDesc(String keyType);
    Optional<JwtKey> findByKid(String kid);

    @Query("select k from JwtKey k where k.keyType = 'ARCHIVED' and (k.expiresAt is null or k.expiresAt > ?1)")
    List<JwtKey> findActiveArchived(OffsetDateTime now);

    @Modifying
    @Transactional
    int deleteByExpiresAtBefore(OffsetDateTime cutoff);
}
```

---

## 3) `KmsProvider.java` (pluggable)
```java
package com.nova.poneglyph.security.kms;

public interface KmsProvider {
    /** Encrypt a small secret (e.g., PKCS#8 private key bytes). */
    byte[] encrypt(byte[] plaintext);
    /** Decrypt ciphertext produced by encrypt(). */
    byte[] decrypt(byte[] ciphertext);
    /** Simple indicator for logs/config. */
    String name();
}
```

### `NoopKmsProvider.java` (fallback)
```java
package com.nova.poneglyph.security.kms;

public class NoopKmsProvider implements KmsProvider {
    @Override public byte[] encrypt(byte[] plaintext) { return plaintext; }
    @Override public byte[] decrypt(byte[] ciphertext) { return ciphertext; }
    @Override public String name() { return "NOOP"; }
}
```

> لاحقاً يمكنك استبدالها بتنفيذ AWS KMS/GCP KMS/Azure Key Vault. الواجهة ثابتة.

---

## 4) `KeyStorageService.java` — thread-safe + caching + persistence + KMS-ready
```java
package com.nova.poneglyph.config.v2;

import com.nova.poneglyph.domain.auth.JwtKey;
import com.nova.poneglyph.repository.JwtKeyRepository;
import com.nova.poneglyph.security.kms.KmsProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class KeyStorageService {
    private static final Logger log = LoggerFactory.getLogger(KeyStorageService.class);
    public static final String ALG = "RS256";

    private final RedisTemplate<String, String> redis;
    private final JwtKeyRepository repo;
    private final KmsProvider kmsProvider; // inject NoopKmsProvider or real KMS

    // Redis keys
    private static final String REDIS_CURRENT_KID = "jwt:current:kid";
    private static final String REDIS_JWKS_CACHE = "jwt:jwks:cache"; // JSON

    /* =================== CREATE / ROTATE =================== */
    @Transactional
    public synchronized JwtKey rotateAndGetNewCurrent(int keySize) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(keySize);
            KeyPair kp = kpg.generateKeyPair();
            RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();
            RSAPublicKey pub = (RSAPublicKey) kp.getPublic();

            String kid = UUID.randomUUID().toString().replace("-", "");
            String privB64 = Base64.getEncoder().encodeToString(priv.getEncoded()); // PKCS#8
            String pubB64  = Base64.getEncoder().encodeToString(pub.getEncoded());  // X.509

            byte[] encPriv = kmsProvider.encrypt(Base64.getDecoder().decode(privB64));
            String encPrivB64 = Base64.getEncoder().encodeToString(encPriv);

            // Move old CURRENT -> ARCHIVED (DB)
            repo.findTopByKeyTypeOrderByCreatedAtDesc("CURRENT").ifPresent(old -> {
                old.setKeyType("ARCHIVED");
                old.setExpiresAt(OffsetDateTime.now().plusDays(30));
                repo.save(old);
            });

            JwtKey current = JwtKey.createCurrent(kid, ALG, encPrivB64, pubB64);
            repo.save(current);

            // Redis quick pointers
            redis.opsForValue().set(REDIS_CURRENT_KID, kid);
            // Invalidate JWKS cache
            redis.delete(REDIS_JWKS_CACHE);
            return current;
        } catch (Exception e) {
            log.error("Key rotation failed: {}", e.getMessage(), e);
            throw new IllegalStateException("Key rotation failed", e);
        }
    }

    /* =================== LOAD CURRENT =================== */
    public Optional<JwtKey> getCurrentKey() {
        try {
            String kid = redis.opsForValue().get(REDIS_CURRENT_KID);
            if (kid != null) {
                return repo.findByKid(kid);
            }
            return repo.findTopByKeyTypeOrderByCreatedAtDesc("CURRENT");
        } catch (Exception e) {
            log.error("Failed to load current key: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /* =================== LOAD BY KID =================== */
    public Optional<JwtKey> getKeyByKid(String kid) {
        return repo.findByKid(kid);
    }

    /* =================== ARCHIVED KEYS =================== */
    @Cacheable(cacheNames = "archivedKeys", key = "'active'", unless = "#result == null")
    public List<JwtKey> getActiveArchivedKeys() {
        return repo.findActiveArchived(OffsetDateTime.now());
    }

    @CacheEvict(cacheNames = "archivedKeys", allEntries = true)
    public void evictArchivedCache() { /* no-op */ }

    /* =================== Materialize RSA keys =================== */
    public RSAPrivateKey toPrivateKey(JwtKey key) {
        try {
            byte[] enc = Base64.getDecoder().decode(key.getPrivateKeyPem());
            byte[] pkcs8 = kmsProvider.decrypt(enc);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pkcs8);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build private key", e);
        }
    }

    public RSAPublicKey toPublicKey(JwtKey key) {
        try {
            byte[] x509 = Base64.getDecoder().decode(key.getPublicKeyPem());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(x509);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build public key", e);
        }
    }

    /* =================== JWKS Cache =================== */
    public Optional<String> getCachedJwksJson() {
        return Optional.ofNullable(redis.opsForValue().get(REDIS_JWKS_CACHE));
    }

    public void cacheJwksJson(String json, long ttlSeconds) {
        redis.opsForValue().set(REDIS_JWKS_CACHE, json, ttlSeconds, TimeUnit.SECONDS);
    }

    /* =================== Housekeeping =================== */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredJwtKeys() {
        int deleted = repo.deleteByExpiresAtBefore(OffsetDateTime.now());
        if (deleted > 0) log.info("Cleaned up {} expired JWT keys", deleted);
        evictArchivedCache();
        redis.delete(REDIS_JWKS_CACHE);
    }
}
```

> **ملاحظة**: لتفعيل caching استعمل Spring Cache (Caffeine/Redis). أضف `@EnableCaching` في config ووفّر bean لـ `KmsProvider` (ابدأ بـ `NoopKmsProvider`).

---

## 5) `JwtUtil.java` — RS256 + kid + archived validation + grace for refresh
```java
package com.nova.poneglyph.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.poneglyph.config.v2.KeyStorageService;
import com.nova.poneglyph.domain.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey; // not used with RSA but kept if needed
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KeyStorageService keys;

    @Value("${jwt.issuer:nova-poneglyph}") private String issuer;
    @Value("${jwt.audience:mobile-app}") private String audience;
    @Value("${jwt.access.expiration:1800}") private long accessExpSec;
    @Value("${jwt.refresh.expiration:1209600}") private long refreshExpSec;
    @Value("${jwt.refresh.expired_grace_seconds:300}") private long refreshGraceSec; // allow parsing recently expired for rotation

    public JwtUtil(KeyStorageService keys) { this.keys = keys; }

    /* =================== Generate =================== */
    public String generateAccessToken(User user) { return generate(user, "access", accessExpSec); }
    public String generateRefreshToken(User user) { return generate(user, "refresh", refreshExpSec); }

    private String generate(User user, String type, long expSeconds) {
        var currentOpt = keys.getCurrentKey();
        if (currentOpt.isEmpty()) throw new IllegalStateException("No CURRENT JWT key available");
        var current = currentOpt.get();
        RSAPrivateKey priv = keys.toPrivateKey(current);

        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .header().keyId(current.getKid()).and()
                .claim("type", type)
                .claim("userId", user.getId().toString())
                .claim("phone", user.getPhoneNumber())
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(user.getId().toString())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith(priv, SignatureAlgorithm.RS256)
                .compact();
    }

    /* =================== Extract =================== */
    public <T> T extractClaim(String token, Function<Claims, T> resolver, boolean allowExpired) {
        try {
            Claims claims = parseClaims(token, allowExpired);
            return resolver.apply(claims);
        } catch (JwtException e) {
            log.debug("extractClaim failed: {}", e.getMessage());
            return null;
        }
    }

    private Claims parseClaims(String token, boolean allowExpired) {
        try {
            JwsHeader<?> hdr = Jwts.parserBuilder().build().parseClaimsJws(token).getHeader();
            String kid = hdr.getKeyId();
            RSAPublicKey pub = resolveVerificationKey(token, kid);
            JwtParser parser = Jwts.parserBuilder().setSigningKey(pub).build();
            return parser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException eje) {
            if (allowExpired) return eje.getClaims();
            throw eje;
        }
    }

    private RSAPublicKey resolveVerificationKey(String token, String kid) {
        // Try by kid current/archived
        if (kid != null) {
            return keys.getKeyByKid(kid)
                    .map(keys::toPublicKey)
                    .or(() -> keys.getActiveArchivedKeys().stream()
                            .filter(k -> kid.equals(k.getKid()))
                            .findFirst()
                            .map(keys::toPublicKey))
                    .orElseGet(() -> tryAllPublicKeys(token));
        }
        // No kid: try all known public keys (current + active archived)
        return tryAllPublicKeys(token);
    }

    private RSAPublicKey tryAllPublicKeys(String token) {
        var all = new ArrayList<>(keys.getActiveArchivedKeys());
        keys.getCurrentKey().ifPresent(all::add);
        for (var k : all) {
            try {
                RSAPublicKey pk = keys.toPublicKey(k);
                Jwts.parserBuilder().setSigningKey(pk).build().parseClaimsJws(token);
                return pk; // first that verifies
            } catch (Exception ignore) { }
        }
        throw new SignatureException("No matching key found for token");
    }

    public String extractJti(String token) { return extractClaim(token, Claims::getId, true); }
    public String extractUserId(String token) { return extractClaim(token, Claims::getSubject, true); }
    public String extractTokenType(String token) { return extractClaim(token, c -> c.get("type", String.class), true); }
    public Date extractExpiration(String token) { return extractClaim(token, Claims::getExpiration, true); }
    public String extractIssuer(String token) { return extractClaim(token, Claims::getIssuer, true); }
    public String extractAudience(String token) {
        return extractClaim(token, c -> {
            Object aud = c.get("aud");
            if (aud instanceof String s) return s;
            if (aud instanceof Collection<?> c2 && !c2.isEmpty()) return String.valueOf(c2.iterator().next());
            try { return c.getAudience(); } catch (Throwable t) { return null; }
        }, true);
    }

    /* =================== Validate =================== */
    private boolean coreValidate(String token, String expectedUserId, String expectedType, boolean allowExpired) {
        try {
            Claims c = parseClaims(token, allowExpired);
            boolean expOk = allowExpired || c.getExpiration().after(new Date());
            String aud = extractAudience(token);
            return expOk
                    && expectedUserId.equals(c.getSubject())
                    && expectedType.equalsIgnoreCase(String.valueOf(c.get("type")))
                    && issuer.equals(c.getIssuer())
                    && audience.equals(aud);
        } catch (JwtException e) {
            log.debug("validate failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateAccessToken(String token, String expectedUserId) {
        return coreValidate(token, expectedUserId, "access", false);
    }

    /**
     * Refresh token may be *recently* expired. Allow parsing expired claims but enforce grace.
     */
    public boolean validateRefreshToken(String token, String expectedUserId) {
        try {
            Claims c = parseClaims(token, true);
            boolean typeOk = "refresh".equalsIgnoreCase(String.valueOf(c.get("type")));
            boolean baseOk = expectedUserId.equals(c.getSubject()) && issuer.equals(c.getIssuer());
            boolean audOk = audience.equals(extractAudience(token));
            boolean timeOk;
            if (c.getExpiration() == null) return false;
            Date now = new Date();
            if (c.getExpiration().after(now)) {
                timeOk = true;
            } else {
                long diff = (now.getTime() - c.getExpiration().getTime()) / 1000L;
                timeOk = diff <= refreshGraceSec; // within grace window
            }
            return typeOk && baseOk && audOk && timeOk;
        } catch (JwtException e) {
            log.debug("refresh validate failed: {}", e.getMessage());
            return false;
        }
    }
}
```

---

## 6) `JwksController.java` — exposes `/.well-known/jwks.json`
```java
package com.nova.poneglyph.web;

import com.nova.poneglyph.config.v2.KeyStorageService;
import com.nova.poneglyph.domain.auth.JwtKey;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class JwksController {
    private final KeyStorageService keys;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String jwks() {
        return keys.getCachedJwksJson().orElseGet(() -> {
            List<Map<String, Object>> jwks = new ArrayList<>();
            List<JwtKey> entries = new ArrayList<>(keys.getActiveArchivedKeys());
            keys.getCurrentKey().ifPresent(entries::add);
            for (JwtKey k : entries) {
                RSAPublicKey pub = keys.toPublicKey(k);
                Map<String, Object> jwk = new LinkedHashMap<>();
                jwk.put("kty", "RSA");
                jwk.put("kid", k.getKid());
                jwk.put("alg", k.getAlg());
                jwk.put("use", "sig");
                jwk.put("n", base64Url(pub.getModulus()));
                jwk.put("e", base64Url(pub.getPublicExponent()));
                jwks.add(jwk);
            }
            String json = "{\"keys\":" + toJson(jwks) + "}";
            keys.cacheJwksJson(json, 60); // cache 60s in Redis
            return json;
        });
    }

    private static String toJson(Object o) {
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(o); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String base64Url(BigInteger v) {
        byte[] bytes = v.toByteArray();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
```

---

## 7) Application wiring (beans & config)
```java
// In a @Configuration class

import com.nova.poneglyph.security.kms.KmsProvider;
import com.nova.poneglyph.security.kms.NoopKmsProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class SecurityBeansConfig {
    @Bean
    public KmsProvider kmsProvider() {
        // TODO: replace with real KMS provider implementation in prod
        return new NoopKmsProvider();
    }
}
```

### `application.properties` (essentials)
```properties
# JWT basics
jwt.issuer=nova-poneglyph
jwt.audience=mobile-app
jwt.access.expiration=1800
jwt.refresh.expiration=1209600
jwt.refresh.expired_grace_seconds=300

# Redis configured elsewhere (spring.redis.*)
```

---

## 8) Notes & Required Adjustments
- **DB Migration**: add columns to `jwt_keys`: `kid`, `alg`, `private_key_pem`, `public_key_pem`. If you already have `secret` from HMAC days، قم بترحيلها إلى RSA مفاتيح جديدة.
- **Initial Key**: call `keyStorageService.rotateAndGetNewCurrent(3072)` once on startup if no CURRENT key exists (e.g., in a `@PostConstruct` of a small initializer).
- **Filter update**: in your `JwtAuthenticationFilter`, replace calls to old `jwtUtil.validateToken` with `validateAccessToken` and keep archived fallback automatically handled by `JwtUtil`.
- **Sessions survive restarts**: ensured if your `RefreshToken`/`Session` live in DB/Redis and validation uses archived keys — nothing in this pack clears sessions on reboot.
- **Backups**: DB backup contains **encrypted** private keys (if KMS is used). Also export public JWKS automatically.
- **Graceful UX**: refresh tokens recently expired (≤ `jwt.refresh.expired_grace_seconds`) can still be parsed and rotated — enables smooth recovery after clock skews or brief downtime.
- **Security**: don’t log raw tokens/keys/OTPs. Log `kid`, `jti`, and userId only.

---

## 9) Optional: Startup Initializer (ensures key exists)
```java
package com.nova.poneglyph.config.v2;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtKeyInitializer {
    private static final Logger log = LoggerFactory.getLogger(JwtKeyInitializer.class);
    private final KeyStorageService keyStorageService;

    @PostConstruct
    public void ensureKey() {
        if (keyStorageService.getCurrentKey().isEmpty()) {
            keyStorageService.rotateAndGetNewCurrent(3072); // or 4096
            log.info("Generated initial CURRENT JWT RSA key");
        }
    }
}
```

---

## 10) Minimal changes in `JwtAuthenticationFilter`
```java
// inside the isValid section, replace validateToken(...)
boolean isValid = jwtUtil.validateAccessToken(jwt, userId);
// archived keys handled internally via kid/public key resolution
```

> For refresh flow in `AuthService`, use `jwtUtil.validateRefreshToken(raw, userId)` which allows a short grace window.

---

### انتهى — الملفات أعلاه جاهزة للوضع في مشروعك.
**إن كان لديك طبقة KMS حقيقية** (AWS/GCP/Azure) أخبرني لأعطيك تنفيذ مزوّد واقعي يأخذ مفاتيح CMK/KEK من الإعدادات. كذلك إن أردت، أقدّم PR تكاملي يعدّل `AuthService`/`JwtAuthenticationFilter` وفق الواجهات الجديدة ويضيف اختبارات وحدات. 

