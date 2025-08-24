package com.nova.poneglyph.config.v2;

import com.nova.poneglyph.domain.auth.JwtKey;
import com.nova.poneglyph.repository.JwtKeyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class KeyStorageService {

    private final Logger log = LoggerFactory.getLogger(KeyStorageService.class);
    private final RedisTemplate<String, String> redisTemplate;

    private final JwtKeyRepository jwtKeyRepository; // مستودع جديد لقاعدة البيانات


    @Transactional
    public void storeCurrentJwtSecret(String secret) {
        try {
            // حفظ في Redis للأداء
            redisTemplate.opsForValue().set("jwt:current:secret", secret);
            redisTemplate.opsForValue().set("jwt:current:rotation",
                    OffsetDateTime.now().toString());

            // التحقق أولاً إذا كان المفتاح موجوداً بالفعل
            Optional<JwtKey> existingKey = jwtKeyRepository.findBySecret(secret);
            if (existingKey.isPresent()) {
                log.debug("Key already exists in database, skipping save");
                return;
            }

            // حفظ في قاعدة البيانات للاستدامة
            // أولاً: تحديث أي مفتاح حالي سابق ليصبح مؤرشفاً
            Optional<JwtKey> currentKeyOpt = jwtKeyRepository.findTopByKeyTypeOrderByCreatedAtDesc("CURRENT");
            if (currentKeyOpt.isPresent()) {
                JwtKey currentKey = currentKeyOpt.get();
                currentKey.setKeyType("ARCHIVED");
                currentKey.setExpiresAt(OffsetDateTime.now().plusDays(30));
                jwtKeyRepository.save(currentKey);
            }

            // ثانياً: حفظ المفتاح الجديد كحالي
            JwtKey newKey = JwtKey.createCurrentKey(secret);
            jwtKeyRepository.save(newKey);

            log.info("JWT key stored successfully in database");
        } catch (Exception e) {
            log.error("Failed to store JWT key in database: {}", e.getMessage(), e);
            // يمكنك إضافة منطق fallback هنا إذا لزم الأمر
        }
    }

    public String getCurrentJwtSecret() {
        try {
            // محاولة جلب من Redis أولاً
            String secret = redisTemplate.opsForValue().get("jwt:current:secret");

            // إذا لم يوجد في Redis، جلب من قاعدة البيانات
            if (secret == null) {
                Optional<JwtKey> currentKey = jwtKeyRepository.findTopByKeyTypeOrderByCreatedAtDesc("CURRENT");
                if (currentKey.isPresent()) {
                    secret = currentKey.get().getSecret();
                    // تخزين في Redis للأداء
                    redisTemplate.opsForValue().set("jwt:current:secret", secret);
                    log.info("Loaded JWT key from database to Redis");
                }
            }

            return secret;
        } catch (Exception e) {
            log.error("Failed to get current JWT key: {}", e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    public void archiveJwtSecret(String secret) {
        try {
            // الأرشيف في Redis
            String key = "jwt:archive:" + OffsetDateTime.now().toEpochSecond();
            redisTemplate.opsForValue().set(key, secret, 30, TimeUnit.DAYS);

            // الأرشيف في قاعدة البيانات
            // التحقق أولاً إذا كان المفتاح موجوداً بالفعل
            Optional<JwtKey> existingKey = jwtKeyRepository.findBySecret(secret);
            if (existingKey.isPresent()) {
                JwtKey keyEntity = existingKey.get();
                if (!"ARCHIVED".equals(keyEntity.getKeyType())) {
                    keyEntity.setKeyType("ARCHIVED");
                    keyEntity.setExpiresAt(OffsetDateTime.now().plusDays(30));
                    jwtKeyRepository.save(keyEntity);
                }
            } else {
                JwtKey archivedKey = JwtKey.createArchivedKey(secret);
                jwtKeyRepository.save(archivedKey);
            }

            log.info("JWT key archived successfully in database");
        } catch (Exception e) {
            log.error("Failed to archive JWT key: {}", e.getMessage(), e);
        }
    }

    public List<String> getArchivedJwtSecrets() {
        List<String> secrets = new ArrayList<>();

        try {
            // الجلب من قاعدة البيانات للاستدامة
            List<JwtKey> archivedKeys = jwtKeyRepository.findByKeyTypeAndExpiresAtAfter(
                    "ARCHIVED", OffsetDateTime.now());

            for (JwtKey key : archivedKeys) {
                secrets.add(key.getSecret());
            }

            log.debug("Retrieved {} archived JWT keys from database", secrets.size());
        } catch (Exception e) {
            log.error("Failed to get archived JWT keys: {}", e.getMessage(), e);
        }

        return secrets;
    }


//    public void storeCurrentJwtSecret(String secret) {
//        redisTemplate.opsForValue().set("jwt:current:secret", secret);
//        redisTemplate.opsForValue().set("jwt:current:rotation",
//                OffsetDateTime.now().toString());
//
//    }

//    public String getCurrentJwtSecret() {
//        return redisTemplate.opsForValue().get("jwt:current:secret");
//    }

//    public void archiveJwtSecret(String secret) {
//        String key = "jwt:archive:" + OffsetDateTime.now().toEpochSecond();
//        redisTemplate.opsForValue().set(key, secret, 30, TimeUnit.DAYS);
//    }

//    public List<String> getArchivedJwtSecrets() {
//        List<String> secrets = new ArrayList<>();
//        Set<String> keys = redisTemplate.keys("jwt:archive:*");
//
//        if (keys != null) {
//            for (String key : keys) {
//                String secret = redisTemplate.opsForValue().get(key);
//                if (secret != null) {
//                    secrets.add(secret);
//                }
//            }
//        }
//
//        return secrets;
//    }

    public void storeCurrentEncryptionKey(String key) {
        redisTemplate.opsForValue().set("encryption:current:key", key);
    }

    public String getCurrentEncryptionKey() {
        return redisTemplate.opsForValue().get("encryption:current:key");
    }

//    public void cleanupExpiredKeys() {
//        // تنظيف المفاتيح المؤرشفة المنتهية الصلاحية
//        Set<String> keys = redisTemplate.keys("jwt:archive:*");
//        if (keys != null) {
//            for (String key : keys) {
//                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
//                if (ttl != null && ttl <= 0) {
//                    redisTemplate.delete(key);
//                }
//            }
//        }
//    }



    public void archiveEncryptionKey(String key) {
        String archiveKey = "encryption:archive:" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(archiveKey, key, 30, TimeUnit.DAYS);
    }

    public List<String> getArchivedEncryptionKeys() {
        List<String> keys = new ArrayList<>();
        Set<String> redisKeys = redisTemplate.keys("encryption:archive:*");

        if (redisKeys != null) {
            for (String key : redisKeys) {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    keys.add(value);
                }
            }
        }
        return keys;
    }

    @Scheduled(cron = "0 0 2 * * ?") // تنظيف يومي الساعة 2 صباحًا
    public void cleanupExpiredKeys() {
        try {
            // تنظيف مفاتيح JWT المؤرشفة المنتهية
            Set<String> jwtArchiveKeys = redisTemplate.keys("jwt:archive:*");
            if (jwtArchiveKeys != null) {
                for (String key : jwtArchiveKeys) {
                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    if (ttl != null && ttl <= 0) {
                        redisTemplate.delete(key);
                    }
                }
            }

            // تنظيف مفاتيح التشفير المؤرشفة المنتهية
            Set<String> encryptionArchiveKeys = redisTemplate.keys("encryption:archive:*");
            if (encryptionArchiveKeys != null) {
                for (String key : encryptionArchiveKeys) {
                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    if (ttl != null && ttl <= 0) {
                        redisTemplate.delete(key);
                    }
                }
            }

            log.info("Expired key cleanup completed");
        } catch (Exception e) {
            log.error("Key cleanup failed", e);
        }
    }
//    public void archiveEncryptionKey(String key) {
//        String archiveKey = "encryption:archive:" + OffsetDateTime.now().toEpochSecond();
//        redisTemplate.opsForValue().set(archiveKey, key, 30, TimeUnit.DAYS);
//    }
//
//    public List<String> getArchivedEncryptionKeys() {
//        List<String> keys = new ArrayList<>();
//        Set<String> redisKeys = redisTemplate.keys("encryption:archive:*");
//
//        if (redisKeys != null) {
//            for (String key : redisKeys) {
//                String value = redisTemplate.opsForValue().get(key);
//                if (value != null) {
//                    keys.add(value);
//                }
//            }
//        }
//        return keys;
//    }
//
//    @Scheduled(cron = "0 0 2 * * ?") // تنظيف يومي الساعة 2 صباحاً
//    public void cleanupExpiredKeys() {
//        try {
//            Set<String> allArchiveKeys = redisTemplate.keys("*:archive:*");
//            if (allArchiveKeys != null) {
//                allArchiveKeys.forEach(key -> {
//                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
//                    if (ttl != null && ttl <= 0) {
//                        redisTemplate.delete(key);
//                    }
//                });
//            }
//        } catch (Exception e) {
//            log.error("Key cleanup failed", e);
//        }
//    }
}
