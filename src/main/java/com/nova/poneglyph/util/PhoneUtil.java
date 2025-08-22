package com.nova.poneglyph.util;



import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class PhoneUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\D");

    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public static String normalizePhone(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return DIGITS_ONLY.matcher(phoneNumber).replaceAll("");
    }

    public String formatPhone(String phoneNumber, String countryCode) {
        if (phoneNumber == null) return null;

        String digits = normalizePhone(phoneNumber);
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }

        return "+" + countryCode + digits;
    }
}
