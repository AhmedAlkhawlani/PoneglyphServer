package com.nova.poneglyph.service.security;

import com.nova.poneglyph.config.v2.KeyStorageService;
import com.nova.poneglyph.util.EncryptionUtil;
import com.nova.poneglyph.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeyRotationService {
    private static final Logger log = LoggerFactory.getLogger(KeyRotationService.class);

    private final EncryptionUtil encryptionUtil;
    private final KeyStorageService keyStorageService;
    private final JwtUtil jwtUtil;

    /**
     * قابل للتهيئة:
     * - حجم مفتاح RSA عند التوليد (بِت)
     * - فترة تدوير مفاتيح JWT (بالمليثانية)
     * - فترة تدوير مفاتيح التشفير (بالمليثانية)
     * - مجلد نسخ احتياطي اختياري لحفظ المفتاح العام / الملف المشفر (آمن فقط إن كان KMS فعالًا)
     */
    @Value("${jwt.rotation.keySize:3072}")
    private int jwtKeySize;

    @Value("${jwt.rotation.interval.millis:86400000}") // افتراضي: يومياً
    private long jwtRotationIntervalMillis;

    @Value("${encryption.rotation.interval.millis:604800000}") // افتراضي: أسبوعياً
    private long encryptionRotationIntervalMillis;

    @Value("${key.backup.dir:}") // إن ترك فارغاً => لا نسخ احتياطي محلي
    private String keyBackupDir;

    @PostConstruct
    public void init() {
        try {
            loadPersistedKeys();
            log.info("KeyRotationService initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize KeyRotationService: {} — will attempt fresh rotation", e.getMessage(), e);
            // محاولة إنشاء مفاتيح جديدة كحل بديل
            try {
                rotateJwtKeysOnce();
            } catch (Exception ex) {
                log.error("Fallback JWT key rotation failed during init: {}", ex.getMessage(), ex);
            }
            try {
                rotateEncryptionKeysOnce();
            } catch (Exception ex) {
                log.error("Fallback encryption key rotation failed during init: {}", ex.getMessage(), ex);
            }
        }
    }

    private void loadPersistedKeys() {
        // Load current JWT key (from Redis or DB via KeyStorageService)
        try {
            Optional<com.nova.poneglyph.domain.auth.JwtKey> currentOpt = keyStorageService.getCurrentKey();
            if (currentOpt.isPresent()) {
                log.info("Loaded CURRENT JWT key (kid={}) from storage", currentOpt.get().getKid());
            } else {
                // لا يوجد مفتاح حالي — أنشئ واحداً
                log.info("No CURRENT JWT key found on startup — generating a new RSA key ({} bits)", jwtKeySize);
                rotateJwtKeysOnce();
            }

            // تحميل المفاتيح المؤرشفة (للمراقبة فقط هنا)
            List<com.nova.poneglyph.domain.auth.JwtKey> archived = keyStorageService.getActiveArchivedKeys();
            log.info("Loaded {} archived JWT keys on startup", archived.size());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load persisted keys", e);
        }
    }

    /**
     * Scheduled rotation — يستخدم KeyStorageService.rotateAndGetNewCurrent
     * نحدد الفاصل عبر property {@code jwt.rotation.interval.millis}
     */
    @Scheduled(fixedRateString = "${jwt.rotation.interval.millis:86400000}")
    @Transactional
    public void rotateJwtKeys() {
        try {
            rotateJwtKeysOnce();
        } catch (Exception e) {
            log.error("JWT key rotation failed", e);
        }
    }

    /**
     * Single-run rotation logic extracted for reuse (ويمكن استدعاؤها من init أو يدوياً)
     */
    @Transactional
    public void rotateJwtKeysOnce() {
        log.info("Starting JWT key rotation (keySize={} bits)", jwtKeySize);
        var previous = keyStorageService.getCurrentKey().orElse(null);
        var newKey = keyStorageService.rotateAndGetNewCurrent(jwtKeySize);
        log.info("Rotated JWT key; new kid={}", newKey.getKid());

        // Optionally backup public key (or encrypted private) if backup dir configured
        if (keyBackupDir != null && !keyBackupDir.isBlank()) {
            try {
                backupKeyToFilesystem(newKey);
            } catch (IOException ioe) {
                log.warn("Failed to backup new JWT key to filesystem: {}", ioe.getMessage(), ioe);
            }
        }

        // Evict caches related to archived keys / jwks handled inside KeyStorageService when rotating
        log.info("JWT key rotation completed. Previous kid={}", previous != null ? previous.getKid() : "none");
    }

    /**
     * Scheduled rotation for encryption keys (application-level encryption keys)
     */
    @Scheduled(fixedRateString = "${encryption.rotation.interval.millis:604800000}")
    @Transactional
    public void rotateEncryptionKeys() {
        try {
            rotateEncryptionKeysOnce();
        } catch (Exception e) {
            log.error("Encryption key rotation failed", e);
        }
    }

    @Transactional
    public void rotateEncryptionKeysOnce() {
        log.info("Starting encryption key rotation");
        String oldKey = keyStorageService.getCurrentEncryptionKey();
        String newKey = EncryptionUtil.generateKey();

        keyStorageService.storeCurrentEncryptionKey(newKey);
        if (oldKey != null) {
            keyStorageService.archiveEncryptionKey(oldKey);
        }
        log.info("Encryption key rotated successfully; archived previous key: {}", oldKey != null);
    }

    private void backupKeyToFilesystem(com.nova.poneglyph.domain.auth.JwtKey key) throws IOException {
        // Backup only public key PEM and encrypted private (as stored). لا تكتب المفتاح الخاص صريحاً إن لم يكن مشفراً
        Path dir = Path.of(keyBackupDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        String ts = OffsetDateTime.now().toString().replace(":", "-");
        String kid = key.getKid();

        // public key
        Path pubFile = dir.resolve("jwt_pub_" + kid + "_" + ts + ".b64");
        Files.writeString(pubFile, key.getPublicKeyPem());
        log.info("Backed up public key to {}", pubFile);

        // private key (may be KMS-encrypted depending on KmsProvider)
        Path privFile = dir.resolve("jwt_priv_enc_" + kid + "_" + ts + ".b64");
        Files.writeString(privFile, key.getPrivateKeyPem());
        log.info("Backed up (possibly encrypted) private key to {}", privFile);
    }

    /**
     * Utility: generate a random base64 secret (kept for backward compatibility if ever needed).
     * Not used for RSA flow.
     */
    private String generateRandomSecret(int bytes) {
        SecureRandom sr = new SecureRandom();
        byte[] buf = new byte[bytes];
        sr.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
    // في KeyRotationService - إضافة تحقق من صحة المفاتيح
    private void validateKey(com.nova.poneglyph.domain.auth.JwtKey key) {
        try {
            RSAPublicKey publicKey = keyStorageService.toPublicKey(key);
            RSAPrivateKey privateKey = keyStorageService.toPrivateKey(key);

            // اختبار توقيع بسيط للتأكد من أن المفتاح يعمل
            String testData = "test";
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(testData.getBytes());
            byte[] signature = sig.sign();

            sig.initVerify(publicKey);
            sig.update(testData.getBytes());
            if (!sig.verify(signature)) {
                throw new IllegalStateException("Key pair validation failed");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Invalid key pair", e);
        }
    }
}
