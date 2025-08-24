package com.nova.poneglyph.service.security;

import com.nova.poneglyph.config.v2.KeyStorageService;
import com.nova.poneglyph.util.EncryptionUtil;
import com.nova.poneglyph.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyRotationService {
    private static final Logger log = LoggerFactory.getLogger(KeyRotationService.class);


    private final EncryptionUtil encryptionUtil;
    private final KeyStorageService keyStorageService;

    private final JwtUtil jwtUtil;

    @PostConstruct
    public void init() {
        try {
            // عند بدء التشغيل، تحميل المفاتيح من التخزين المستدام
            loadPersistedKeys();
            log.info("KeyRotationService initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize KeyRotationService: {}", e.getMessage(), e);
            // في حالة الفشل، إنشاء مفاتيح جديدة
            rotateJwtKeys();
            rotateEncryptionKeys();
        }
    }

    private void loadPersistedKeys() {
        try {
            // تحميل المفتاح الحالي من التخزين المستدام
            String currentSecret = keyStorageService.getCurrentJwtSecret();
            if (currentSecret != null) {
                jwtUtil.updateSigningKey(currentSecret);
                log.info("Loaded persisted JWT key on startup");
            } else {
                // إذا لم يوجد مفتاح، إنشاء واحد جديد
                log.info("No persisted JWT key found, generating new one");
                rotateJwtKeys();
            }

            // تحميل المفاتيح المؤرشفة
            List<String> archivedSecrets = keyStorageService.getArchivedJwtSecrets();
            log.info("Loaded {} archived JWT keys on startup", archivedSecrets.size());

        } catch (Exception e) {
            log.error("Failed to load persisted keys on startup: {}", e.getMessage(), e);
            rotateJwtKeys(); // إنشاء مفتاح جديد كحل بديل
        }
    }



    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void rotateJwtKeys() {
        try {
            String oldSecret = keyStorageService.getCurrentJwtSecret();
            String newSecret = generateNewSecret();

            keyStorageService.storeCurrentJwtSecret(newSecret);
            jwtUtil.updateSigningKey(newSecret);

            if (oldSecret != null) {
                keyStorageService.archiveJwtSecret(oldSecret);
                log.info("JWT key rotated successfully");
            }
        } catch (Exception e) {
            log.error("JWT key rotation failed", e);
        }
    }

    @Scheduled(fixedRate = 604800000)
    @Transactional
    public void rotateEncryptionKeys() {
        try {
            String oldKey = keyStorageService.getCurrentEncryptionKey();
            String newKey = EncryptionUtil.generateKey();

            keyStorageService.storeCurrentEncryptionKey(newKey);

            if (oldKey != null) {
                keyStorageService.archiveEncryptionKey(oldKey);
                log.info("Encryption key rotated successfully");
            }
        } catch (Exception e) {
            log.error("Encryption key rotation failed", e);
        }
    }

    private String generateNewSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[64];
        secureRandom.nextBytes(keyBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
    }
    // تدوير مفاتيح JWT كل 24 ساعة
//    @Scheduled(fixedRate = 86400000)
//    @Transactional
//    public void rotateJwtKeys() {
//        String newSecret = generateNewSecret();
//        keyStorageService.storeCurrentJwtSecret(newSecret);
//        jwtUtil.updateSigningKey(newSecret);
//
//        // الاحتفاظ بالمفاتيح القديمة لفترة للتحقق من التوكنات السابقة
//        keyStorageService.archiveJwtSecret(keyStorageService.getCurrentJwtSecret());
//    }

//    @Scheduled(fixedRate = 86400000)
//    @Transactional
//    public void rotateJwtKeys() {
//        String old = keyStorageService.getCurrentJwtSecret(); // خذ القديم
//        String newSecret = generateNewSecret();
//        keyStorageService.storeCurrentJwtSecret(newSecret);   // ضع الجديد
//        jwtUtil.updateSigningKey(newSecret);
//        if (old != null) {
//            keyStorageService.archiveJwtSecret(old);         // ثم أرشف القديم
//        }
//    }


    // تدوير مفاتيح التشفير كل 7 أيام
//    @Scheduled(fixedRate = 604800000)
//    @Transactional
//    public void rotateEncryptionKeys() {
//        String newKey = EncryptionUtil.generateKey();
//        keyStorageService.storeCurrentEncryptionKey(newKey);
//        // لا يمكن تحديث مفتاح التشفير الحالي مباشرةً،
//        // ولكن يمكن استخدامه للتشفير الجديد بينما تبقى المفاتيح القديمة للفك
//    }

//    private String generateNewSecret() {
//        // توليد سر جديد آمن لـ JWT
//        byte[] keyBytes = new byte[64]; // 512-bit
//        new java.security.SecureRandom().nextBytes(keyBytes);
//        return Base64.getEncoder().encodeToString(keyBytes);
//    }
}
