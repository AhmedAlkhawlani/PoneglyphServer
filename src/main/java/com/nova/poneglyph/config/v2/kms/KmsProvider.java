package com.nova.poneglyph.config.v2.kms;


public interface KmsProvider {
    /** Encrypt a small secret (e.g., PKCS#8 private key bytes). */
    byte[] encrypt(byte[] plaintext);
    /** Decrypt ciphertext produced by encrypt(). */
    byte[] decrypt(byte[] ciphertext);
    /** Simple indicator for logs/config. */
    String name();
}
