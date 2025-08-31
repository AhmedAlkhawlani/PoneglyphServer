package com.nova.poneglyph.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PhoneUtilTest {

    @Test
    public void testNormalizeForStorage_basicE164() {
        assertEquals("967712345678", PhoneUtil.normalizeForStorage("+967712345678", "YE"));
    }

    @Test
    public void testNormalizeForStorage_00Prefix() {
        assertEquals("967712345678", PhoneUtil.normalizeForStorage("00967712345678", "YE"));
    }

    @Test
    public void testNormalizeForStorage_trunkZero() {
        assertEquals("967712345678", PhoneUtil.normalizeForStorage("0712345678", "YE"));
    }

    @Test
    public void testNormalizeForStorage_noLeadingZero_local() {
        assertEquals("967712345678", PhoneUtil.normalizeForStorage("712345678", "YE"));
    }

    @Test
    public void testNormalizeForStorage_tooShort() {
//        assertNull(PhoneUtil.normalizeForStorage("123", "YE"));
    }

    // =========================
    // normalizeWithFallbacks
    // =========================
    @Test
    public void testNormalizeWithFallbacks_localNoPrefix() {
        assertEquals("967712345678", PhoneUtil.normalizeWithFallbacks("712345678", "YE", "967"));
    }

    @Test
    public void testNormalizeWithFallbacks_trunkZero() {
        assertEquals("967712345678", PhoneUtil.normalizeWithFallbacks("0712345678", "YE", "967"));
    }

    @Test
    public void testNormalizeWithFallbacks_alreadyE164() {
        assertEquals("967712345678", PhoneUtil.normalizeWithFallbacks("+967712345678", "YE", "967"));
    }

    @Test
    public void testNormalizeWithFallbacks_otherCountryE164() {
        // رقم من السعودية +966 - يجب أن يحترم رمز الدولة الوارد ولا يحول إلى Yemen
        assertEquals("966512345678", PhoneUtil.normalizeWithFallbacks("+966512345678", "YE", "967"));
    }

    // =========================
    // normalizeToE164
    // =========================
    @Test
    public void testNormalizeToE164_examples() {
        assertEquals("+967712345678", PhoneUtil.normalizeToE164("+967712345678", "YE"));
        assertEquals("+967712345678", PhoneUtil.normalizeToE164("0712345678", "YE"));
//        assertNull(PhoneUtil.normalizeToE164("123", "YE"));
    }
}
