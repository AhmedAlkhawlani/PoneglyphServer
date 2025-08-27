//package com.nova.poneglyph.util;
//
//
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.util.Base64;
//
//
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.GCMParameterSpec;
//import java.security.SecureRandom;
//
//@Component
//public class EncryptionUtil {
//
//    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
//    private static final int GCM_TAG_LENGTH = 128;
//    private static final int GCM_IV_LENGTH = 12;
//
//    public String encrypt(String plaintext, String key) {
//        try {
//            byte[] iv = new byte[GCM_IV_LENGTH];
//            new SecureRandom().nextBytes(iv);
//
//            SecretKey secretKey = generateKey(key);
//            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
//            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
//            byte[] encryptedText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
//
//            byte[] combined = new byte[iv.length + encryptedText.length];
//            System.arraycopy(iv, 0, combined, 0, iv.length);
//            System.arraycopy(encryptedText, 0, combined, iv.length, encryptedText.length);
//
//            return Base64.getEncoder().encodeToString(combined);
//        } catch (Exception e) {
//            throw new RuntimeException("Encryption failed", e);
//        }
//    }
//
//    public String decrypt(String ciphertext, String key) {
//        try {
//            byte[] combined = Base64.getDecoder().decode(ciphertext);
//            byte[] iv = new byte[GCM_IV_LENGTH];
//            byte[] encryptedText = new byte[combined.length - GCM_IV_LENGTH];
//
//            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
//            System.arraycopy(combined, GCM_IV_LENGTH, encryptedText, 0, encryptedText.length);
//
//            SecretKey secretKey = generateKey(key);
//            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
//            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
//            byte[] decryptedText = cipher.doFinal(encryptedText);
//
//            return new String(decryptedText, StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            throw new RuntimeException("Decryption failed", e);
//        }
//    }
//
//    public String hash(String data) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
//            return Base64.getEncoder().encodeToString(hash);
//        } catch (Exception e) {
//            throw new RuntimeException("Hashing failed", e);
//        }
//    }
//
//    public static String generateKey() {
//        SecureRandom random = new SecureRandom();
//        byte[] key = new byte[32]; // 256 bits
//        random.nextBytes(key);
//        return Base64.getEncoder().encodeToString(key);
//    }
//
//    private SecretKey generateKey(String keyString) {
//        byte[] decodedKey = Base64.getDecoder().decode(keyString);
//        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
//    }
//}
//
//package com.nova.poneglyph.util;
//
//import org.springframework.stereotype.Component;
//
//import javax.crypto.Cipher;
//import javax.crypto.KeyGenerator;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.GCMParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.SecureRandom;
//import java.util.Base64;
//
//@Component
//public final class EncryptionUtil {
//
//    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
//    private static final int GCM_TAG_LENGTH = 128; // bits
//    private static final int GCM_IV_LENGTH = 12;   // bytes
//    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
//
//    /**
//     * يولّد مفتاح AES 256-bit ويرجعه Base64.
//     */
//    public static String generateKey() {
//        try {
//            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
//            keyGen.init(256);
//            SecretKey key = keyGen.generateKey();
//            return Base64.getEncoder().encodeToString(key.getEncoded());
//        } catch (Exception e) {
//            throw new RuntimeException("Key generation failed", e);
//        }
//    }
//
//    /**
//     * تشفير نص بـ AES-GCM. المفتاح يجب أن يكون Base64 لمفتاح خام (256-bit).
//     * ترجع Base64(iv || ciphertext).
//     */
//    public String encrypt(String plaintext, String base64Key) {
//        try {
//            byte[] keyBytes = decodeAndValidateKey(base64Key);
//            byte[] iv = new byte[GCM_IV_LENGTH];
//            SECURE_RANDOM.nextBytes(iv);
//
//            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
//            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
//            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
//
//            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
//
//            ByteBuffer bb = ByteBuffer.allocate(iv.length + ciphertext.length);
//            bb.put(iv);
//            bb.put(ciphertext);
//            return Base64.getEncoder().encodeToString(bb.array());
//        } catch (Exception e) {
//            throw new RuntimeException("Encryption failed", e);
//        }
//    }
//
//    /**
//     * فك تشفير Base64(iv || ciphertext) بـ AES-GCM.
//     */
//    public String decrypt(String base64IvCiphertext, String base64Key) {
//        try {
//            byte[] keyBytes = decodeAndValidateKey(base64Key);
//            byte[] ivCipher = Base64.getDecoder().decode(base64IvCiphertext);
//
//            if (ivCipher.length < GCM_IV_LENGTH + 1) {
//                throw new IllegalArgumentException("Ciphertext too short");
//            }
//
//            ByteBuffer bb = ByteBuffer.wrap(ivCipher);
//            byte[] iv = new byte[GCM_IV_LENGTH];
//            bb.get(iv);
//            byte[] ciphertext = new byte[bb.remaining()];
//            bb.get(ciphertext);
//
//            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
//            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
//            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
//            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
//
//            byte[] plaintext = cipher.doFinal(ciphertext);
//            return new String(plaintext, StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            throw new RuntimeException("Decryption failed", e);
//        }
//    }
//
//    /**
//     * SHA-256 (Base64) — مفيدة لتخزين refresh tokens كهاش.
//     */
//    public String hash(String data) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
//            return Base64.getEncoder().encodeToString(hash);
//        } catch (Exception e) {
//            throw new RuntimeException("Hashing failed", e);
//        }
//    }
//
//    private static byte[] decodeAndValidateKey(String base64Key) {
//        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
//        // نقبل فقط 256-bit (32 بايت). يمكنك السماح بـ 16/24 حسب حاجتك.
//        if (decodedKey.length != 32) {
//            throw new IllegalArgumentException("Invalid AES key length. Expected 32 bytes (256-bit). Got: " + decodedKey.length);
//        }
//        return decodedKey;
//    }
//}
// File: src/main/java/com/nova/poneglyph/util/EncryptionUtil.java
package com.nova.poneglyph.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utilities for AES-GCM encryption/decryption. Returns base64 encoded iv+ciphertext.
 */
@Component
public final class EncryptionUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // bits

    private static final int SALT_LENGTH = 16;
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    /**
     * AES-GCM encryption with base64 output
     */
    public String encrypt(String plaintext, String base64Key) {
        try {
            byte[] keyBytes = decodeAndValidateKey(base64Key);
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(iv.length + ciphertext.length);
            bb.put(iv);
            bb.put(ciphertext);
            return Base64.getEncoder().encodeToString(bb.array());
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * AES-GCM decryption with base64 input
     */
    public String decrypt(String base64IvCiphertext, String base64Key) {
        try {
            byte[] keyBytes = decodeAndValidateKey(base64Key);
            byte[] ivCipher = Base64.getDecoder().decode(base64IvCiphertext);

            if (ivCipher.length < GCM_IV_LENGTH + 1) {
                throw new IllegalArgumentException("Ciphertext too short");
            }

            ByteBuffer bb = ByteBuffer.wrap(ivCipher);
            byte[] iv = new byte[GCM_IV_LENGTH];
            bb.get(iv);
            byte[] ciphertext = new byte[bb.remaining()];
            bb.get(ciphertext);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
    /// الأفضل – تخزين بايتات خام

    public byte[] encryptToBytes(String plaintext, String base64Key) {
        try {
            byte[] keyBytes = decodeAndValidateKey(base64Key);
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(iv.length + ciphertext.length);
            bb.put(iv);
            bb.put(ciphertext);
            return bb.array(); // ✅ byte[]
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }


    /**
     * SHA-256 hashing with base64 output
     */
    public String hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    /**
     * Generate a new AES key (256-bit)
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Key generation failed", e);
        }
    }

    /**
     * Generate AES key with specific bit length
     */
    public static SecretKey generateAesKey(int bits) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(bits);
        return keyGen.generateKey();
    }

    /**
     * Low-level AES-GCM encryption
     */
    public static String encryptAesGcmBase64(byte[] keyBytes, byte[] plaintext) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
        byte[] ciphertext = cipher.doFinal(plaintext);

        ByteBuffer bb = ByteBuffer.allocate(iv.length + ciphertext.length);
        bb.put(iv);
        bb.put(ciphertext);
        return Base64.getEncoder().encodeToString(bb.array());
    }

    /**
     * Low-level AES-GCM decryption
     */
    public static byte[] decryptAesGcmBase64(byte[] keyBytes, String base64IvCiphertext) throws Exception {
        byte[] ivCipher = Base64.getDecoder().decode(base64IvCiphertext);
        ByteBuffer bb = ByteBuffer.wrap(ivCipher);
        byte[] iv = new byte[GCM_IV_LENGTH];
        bb.get(iv);
        byte[] ciphertext = new byte[bb.remaining()];
        bb.get(ciphertext);

        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
        return cipher.doFinal(ciphertext);
    }

    private static byte[] decodeAndValidateKey(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        if (decodedKey.length != 32) {
            throw new IllegalArgumentException("Invalid AES key length. Expected 32 bytes (256-bit). Got: " + decodedKey.length);
        }
        return decodedKey;
    }

    /**
     * تشفير النص باستخدام كلمة مرور (PBKDF2 مع AES-GCM)
     */
    public String encryptWithPassword(String plaintext, String password) {
        try {
            // إنشاء salt عشوائي
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);

            // اشتقاق مفتاح من كلمة المرور
            SecretKey secretKey = deriveKey(password, salt);

            // إنشاء IV عشوائي
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // التشفير
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // دمج salt + iv + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(SALT_LENGTH + GCM_IV_LENGTH + ciphertext.length);
            byteBuffer.put(salt);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Encryption with password failed", e);
        }
    }

    /**
     * فك تشفير النص باستخدام كلمة مرور
     */
    public String decryptWithPassword(String ciphertext, String password) {
        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            // استخراج salt و iv و ciphertext
            byte[] salt = new byte[SALT_LENGTH];
            byteBuffer.get(salt);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedText = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedText);

            // اشتقاق مفتاح من كلمة المرور
            SecretKey secretKey = deriveKey(password, salt);

            // فك التشفير
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plaintext = cipher.doFinal(encryptedText);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption with password failed", e);
        }
    }
    /// الأفضل – تخزين بايتات خام
    public String decryptFromBytes(byte[] ivCiphertext, String base64Key) {
        try {
            byte[] keyBytes = decodeAndValidateKey(base64Key);

            ByteBuffer bb = ByteBuffer.wrap(ivCiphertext);
            byte[] iv = new byte[GCM_IV_LENGTH];
            bb.get(iv);
            byte[] ciphertext = new byte[bb.remaining()];
            bb.get(ciphertext);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8); // ✅ String بعد فك التشفير
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }


    /**
     * اشتقاق مفتاح من كلمة المرور باستخدام PBKDF2
     */
    private SecretKey deriveKey(String password, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }
}
