package com.nova.poneglyph.config.v2;

import com.nova.poneglyph.config.v2.kms.KmsProvider;
import com.nova.poneglyph.domain.auth.JwtKey;
import com.nova.poneglyph.repository.JwtKeyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    // Encryption key redis keys/patterns
    private static final String REDIS_ENC_CURRENT = "encryption:current:key";
    private static final String REDIS_ENC_ARCHIVE_PREFIX = "encryption:archive:"; // + timestamp

    /* =================== CREATE / ROTATE (JWT RSA) =================== */
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

    /* =================== ARCHIVED KEYS (JWT) =================== */
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

    /* =================== Encryption Key management (Redis) =================== */

    /**
     * Store the current encryption key (fast lookup in Redis).
     * This is intended for application-level encryption keys (not RSA JWT keys).
     * If you need the encryption key to be persisted, ensure Redis is persistent or plug a DB-backed store.
     */
    @Transactional
    public void storeCurrentEncryptionKey(String key) {
        try {
            if (key == null) return;
            redis.opsForValue().set(REDIS_ENC_CURRENT, key);
            log.info("Stored current encryption key in Redis (length={})", key.length());
        } catch (Exception e) {
            log.error("Failed to store current encryption key: {}", e.getMessage(), e);
        }
    }

    /**
     * Get the current encryption key from Redis.
     */
    public String getCurrentEncryptionKey() {
        try {
            return redis.opsForValue().get(REDIS_ENC_CURRENT);
        } catch (Exception e) {
            log.error("Failed to get current encryption key: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Archive an encryption key (store in Redis with TTL and keep for recovery).
     * Key will be stored under key prefix 'encryption:archive:<epochSeconds>' with TTL 30 days.
     */
    @Transactional
    public void archiveEncryptionKey(String key) {
        try {
            if (key == null) return;
            String redisKey = REDIS_ENC_ARCHIVE_PREFIX + OffsetDateTime.now().toEpochSecond();
            redis.opsForValue().set(redisKey, key, 30, TimeUnit.DAYS);
            log.info("Archived encryption key into Redis with key {}", redisKey);
        } catch (Exception e) {
            log.error("Failed to archive encryption key: {}", e.getMessage(), e);
        }
    }

    /**
     * Return list of archived encryption keys currently present in Redis (values).
     * Note: uses redis.keys which might be expensive on large datasets.
     */
    public List<String> getArchivedEncryptionKeys() {
        List<String> keysList = new ArrayList<>();
        try {
            Set<String> redisKeys = redis.keys(REDIS_ENC_ARCHIVE_PREFIX + "*");
            if (redisKeys != null) {
                for (String k : redisKeys) {
                    String val = redis.opsForValue().get(k);
                    if (val != null) keysList.add(val);
                }
            }
        } catch (Exception e) {
            log.error("Failed to get archived encryption keys: {}", e.getMessage(), e);
        }
        return keysList;
    }

    /* =================== Housekeeping =================== */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredJwtKeys() {
        int deleted = repo.deleteByExpiresAtBefore(OffsetDateTime.now());
        if (deleted > 0) log.info("Cleaned up {} expired JWT keys", deleted);
        evictArchivedCache();
        redis.delete(REDIS_JWKS_CACHE);

        // cleanup encryption archives with expired TTL (Redis handles TTL automatically),
        // but we will also ensure any keys with ttl <= 0 are removed (defensive).
        try {
            Set<String> encArchiveKeys = redis.keys(REDIS_ENC_ARCHIVE_PREFIX + "*");
            if (encArchiveKeys != null) {
                for (String key : encArchiveKeys) {
                    Long ttl = redis.getExpire(key, TimeUnit.SECONDS);
                    if (ttl != null && ttl <= 0) {
                        redis.delete(key);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to cleanup encryption archive keys: {}", e.getMessage(), e);
        }
    }
}
