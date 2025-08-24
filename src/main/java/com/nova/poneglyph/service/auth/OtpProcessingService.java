package com.nova.poneglyph.service.auth;



import com.nova.poneglyph.domain.auth.OtpCode;
import com.nova.poneglyph.exception.OtpValidationException;
import com.nova.poneglyph.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class OtpProcessingService {

    private final OtpCodeRepository otpCodeRepository;
    private final PasswordEncoder passwordEncoder;

    // قيمة maxOtpAttempts يمكن تمريرها أو قراءتها من الخصائص
    private final int maxOtpAttempts = 5; // أو اجعلها @Value

    public enum OtpCheckResult {
        SUCCESS, INVALID, TOO_MANY_ATTEMPTS, EXPIRED, NOT_FOUND
    }

    public static boolean safeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length() && i < b.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0 && a.length() == b.length();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OtpCheckResult processOtpAttemptAndMaybeConsume(Long otpId, String providedCode) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Optional<OtpCode> maybe = otpCodeRepository.findByIdForUpdate(otpId);
        if (maybe.isEmpty()) {
            log.debug("OTP not found id={}", otpId);
            return OtpCheckResult.NOT_FOUND;
        }

        OtpCode otp = maybe.get();

        if (otp.isUsed()) {
            log.debug("OTP already used id={}", otpId);
            return OtpCheckResult.EXPIRED;
        }
        if (otp.getExpiresAt() == null || otp.getExpiresAt().isBefore(now)) {
            otp.setUsed(true);
            otpCodeRepository.save(otp);
            return OtpCheckResult.EXPIRED;
        }

        // استبدال المقارنة العادية بالمقارنة الآمنة
//        if (!safeEquals(storedOtp.getCodeHash(), hashedProvidedCode)) {
            // ... handle invalid code ...
//        }
        boolean matches = passwordEncoder.matches(providedCode, otp.getCodeHash());
        if (matches) {
            otp.setUsed(true);
            otpCodeRepository.save(otp);
            return OtpCheckResult.SUCCESS;
        } else {
            int attempts = otp.getAttempts() + 1;
            otp.setAttempts(attempts);
            if (attempts >= maxOtpAttempts) {
                otp.setUsed(true);
                otpCodeRepository.save(otp);
                return OtpCheckResult.TOO_MANY_ATTEMPTS;
            } else {
                otpCodeRepository.save(otp);
                return OtpCheckResult.INVALID;
            }
        }
    }
}

