package com.nova.poneglyph.util;



import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class E2EEUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(256);
        return keyGen.generateKeyPair();
    }

    public static SecretKey deriveSharedSecret(PrivateKey privateKey, PublicKey publicKey)
            throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return new SecretKeySpec(keyAgreement.generateSecret(), "AES");
    }

    public static String encryptMessage(String message, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] encrypted = cipher.doFinal(message.getBytes());
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decryptMessage(String encryptedMessage, SecretKey key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedMessage);
        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[combined.length - IV_LENGTH];

        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
        System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }

    public static PublicKey decodePublicKey(String encoded) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(encoded);
        return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(bytes));
    }

    public static String encodeKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
