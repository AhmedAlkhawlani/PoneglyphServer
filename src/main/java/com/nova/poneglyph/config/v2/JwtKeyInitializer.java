package com.nova.poneglyph.config.v2;

import com.nova.poneglyph.domain.auth.JwtKey;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
@RequiredArgsConstructor
public class JwtKeyInitializer {
    private static final Logger log = LoggerFactory.getLogger(JwtKeyInitializer.class);
    private final KeyStorageService keyStorageService;

//    @PostConstruct
//    public void ensureKey() {
//        if (keyStorageService.getCurrentKey().isEmpty()) {
//            keyStorageService.rotateAndGetNewCurrent(3072); // or 4096
//            log.info("Generated initial CURRENT JWT RSA key");
//        }
//    }
    // في JwtKeyInitializer - إضافة تحقق إضافي
    @PostConstruct
    public void ensureKey() {
        try {
            if (keyStorageService.getCurrentKey().isEmpty()) {
                JwtKey newKey = keyStorageService.rotateAndGetNewCurrent(3072);
                log.info("Generated initial CURRENT JWT RSA key with kid: {}", newKey.getKid());

                // التحقق من أن المفتاح يعمل
                validateKey(newKey);
            } else {
                // التحقق من أن المفتاح الحالي صالح
                keyStorageService.getCurrentKey().ifPresent(this::validateKey);
            }
        } catch (Exception e) {
            log.error("Failed to initialize JWT keys: {}", e.getMessage(), e);
            throw new IllegalStateException("JWT key initialization failed", e);
        }
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
