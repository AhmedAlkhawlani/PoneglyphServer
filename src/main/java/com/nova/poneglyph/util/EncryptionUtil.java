package com.nova.poneglyph.util;



import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
//
//public class EncryptionUtil {
//
//    private static final String ALGORITHM = "AES";
//
//    public static String generateKey() {
//        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
//    }
//
//    public static String encrypt(String message, String secretKey) {
//        try {
//            SecretKeySpec key = prepareKey(secretKey);
//            Cipher cipher = Cipher.getInstance(ALGORITHM);
//            cipher.init(Cipher.ENCRYPT_MODE, key);
//            byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
//            return Base64.getEncoder().encodeToString(encrypted);
//        } catch (Exception e) {
//            throw new RuntimeException("Encryption failed", e);
//        }
//    }
//
//    public static String decrypt(String encryptedMessage, String secretKey) {
//        try {
//            SecretKeySpec key = prepareKey(secretKey);
//            Cipher cipher = Cipher.getInstance(ALGORITHM);
//            cipher.init(Cipher.DECRYPT_MODE, key);
//            byte[] decoded = Base64.getDecoder().decode(encryptedMessage);
//            byte[] decrypted = cipher.doFinal(decoded);
//            return new String(decrypted, StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            throw new RuntimeException("Decryption failed", e);
//        }
//    }
//
//    private static SecretKeySpec prepareKey(String secretKey) throws Exception {
//        byte[] key = secretKey.getBytes(StandardCharsets.UTF_8);
//        MessageDigest sha = MessageDigest.getInstance("SHA-256");
//        key = sha.digest(key);
//        key = Arrays.copyOf(key, 16); // AES-128
//        return new SecretKeySpec(key, ALGORITHM);
//    }
//}


import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

@Component
public class EncryptionUtil {

    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    public String encrypt(String plaintext, String key) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            SecretKey secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] encryptedText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedText, 0, combined, iv.length, encryptedText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String ciphertext, String key) {
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encryptedText = new byte[combined.length - GCM_IV_LENGTH];

            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedText, 0, encryptedText.length);

            SecretKey secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] decryptedText = cipher.doFinal(encryptedText);

            return new String(decryptedText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public String hash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    public static String generateKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32]; // 256 bits
        random.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    private SecretKey generateKey(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
