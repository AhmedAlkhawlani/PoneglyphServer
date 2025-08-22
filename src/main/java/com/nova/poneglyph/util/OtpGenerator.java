package com.nova.poneglyph.util;



import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class OtpGenerator {

    private static final String NUMBERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generate(int length) {
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        return otp.toString();
    }

    public static String generateAlphanumeric(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(characters.charAt(random.nextInt(characters.length())));
        }
        return otp.toString();
    }
}
