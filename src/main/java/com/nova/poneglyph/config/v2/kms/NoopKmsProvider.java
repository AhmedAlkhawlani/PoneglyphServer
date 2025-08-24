package com.nova.poneglyph.config.v2.kms;


public class NoopKmsProvider implements KmsProvider {
    @Override public byte[] encrypt(byte[] plaintext) { return plaintext; }
    @Override public byte[] decrypt(byte[] ciphertext) { return ciphertext; }
    @Override public String name() { return "NOOP"; }
}
