//
//package com.nova.poneglyph.service.auth;
//
//import com.nova.poneglyph.domain.auth.OtpCode;
//import com.nova.poneglyph.domain.auth.RefreshToken;
//import com.nova.poneglyph.domain.enums.AccountStatus;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.domain.user.UserSession;
//import com.nova.poneglyph.dto.authDto.AuthResponseDto;
//import com.nova.poneglyph.dto.authDto.OtpRequestDto;
//import com.nova.poneglyph.dto.authDto.OtpVerifyDto;
//import com.nova.poneglyph.dto.authDto.RefreshRequestDto;
//import com.nova.poneglyph.exception.*;
//import com.nova.poneglyph.repository.*;
//import com.nova.poneglyph.util.JwtUtil;
//import com.nova.poneglyph.util.OtpGenerator;
//import com.nova.poneglyph.util.PhoneUtil;
//import com.nova.poneglyph.util.RateLimiterService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Duration;
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//
//
//@Log4j2
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final OtpCodeRepository otpCodeRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final UserSessionRepository userSessionRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtUtil jwtUtil;
//    private final PhoneUtil phoneUtil;
//    private final RateLimiterService rateLimiterService;
//    // إضافة الحقل
//    private final OtpProcessingService otpProcessingService;
//
//
//    @Value("${jwt.access.expiration}")
//    private long accessExpiration;
//
//    @Value("${jwt.refresh.expiration}")
//    private long refreshExpiration;
//
//    @Value("${otp.expiration.minutes}")
//    private int otpExpirationMinutes;
//
//    @Value("${otp.max-attempts}")
//    private int maxOtpAttempts;
//
//    /**
//     * طلب إنشاء OTP — تحفظ السجل في DB. هذه العملية عادية ضمن TRANSACTION واحد.
//     */
//    @Transactional
//    public void requestOtp(OtpRequestDto requestDto) {
//        String normalized = PhoneUtil.normalizePhone(requestDto.getPhone());
//
//        if (rateLimiterService.isRateLimited("otp_request:" + normalized, 8, Duration.ofMinutes(15))) {
//            throw new RateLimitExceededException("Too many OTP requests");
//        }
//
//        // استخدم UTC لتوحيد الزمن
//        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
//        OffsetDateTime expiresAt = now.plusMinutes(otpExpirationMinutes);
//
//        log.debug("Creating OTP - Now (UTC): {}, Expires (UTC): {}", now, expiresAt);
//
//        String otp = OtpGenerator.generate(6);
//        log.debug("Generated OTP (plain) for debug/testing: {}", otp); // احذر من ترك هذا في الإنتاج
//
//        String otpHash = passwordEncoder.encode(otp);
//
//        OtpCode otpCode = OtpCode.builder()
//                .phoneNumber(requestDto.getPhone())
//                .normalizedPhone(normalized)
//                .codeHash(otpHash)
//                .expiresAt(expiresAt)
//                .requesterIp(requestDto.getIp())
//                .deviceFingerprint(requestDto.getDeviceFingerprint())
//                .createdAt(now)
//                .attempts(0)
//                .used(false)
//                .build();
//
//        OtpCode saved = otpCodeRepository.save(otpCode);
//        log.debug("OTP saved successfully with ID: {}", saved.getId());
//
//        // TODO: integration with real SMS provider (do not log OTP in production)
//        // smsService.sendOtp(requestDto.getPhone(), otp);
//    }
//
//    /**
//     * التحقق من OTP: تصميم آمن ضد rollback و race conditions.
//     *
//     * ملاحظات مهمة:
//     * - لا نعتمد على أن عملية validateOtp... سترمي exception وتبقى التغييرات؛
//     *   بدلًا من ذلك الدوال الفرعية تعمل في REQUIRES_NEW وتؤكد(commit) التغيرات.
//     * - verifyOtp يقرر بناءً على نتيجة الدوال الفرعية إن يستمر أو يرمي استثناء.
//     */
//    public AuthResponseDto verifyOtp(OtpVerifyDto verifyDto) {
//        String normalized = PhoneUtil.normalizePhone(verifyDto.getPhone());
//        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
//
//        log.debug("verifyOtp called for phone={} normalized={} at {}", verifyDto.getPhone(), normalized, now);
//
//        // اقرأ أحدث OTP صالح (بدون قفل هنا — نعيد تحميله داخل المعاملة الفرعية مع قفل)
//        Optional<OtpCode> otpOptional = otpCodeRepository.findLatestValidOtp(normalized, now);
//        if (otpOptional.isEmpty()) {
//            log.warn("No valid OTP found for phone {}. Full list follow...", normalized);
//            List<OtpCode> all = otpCodeRepository.findByNormalizedPhoneOrderByCreatedAtDesc(normalized);
//            if (all.isEmpty()) {
//                log.warn("No OTP records for {}", normalized);
//            } else {
//                for (OtpCode o : all) {
//                    log.warn("OTP id={}, expiresAt={}, used={}, attempts={}, createdAt={}",
//                            o.getId(), o.getExpiresAt(), o.isUsed(), o.getAttempts(), o.getCreatedAt());
//                }
//            }
//            throw new OtpValidationException("Invalid or expired OTP");
//        }
//
//        OtpCode otpCode = otpOptional.get();
//
//        // Process atomically (REQUIRES_NEW) with DB row lock to prevent race conditions
////        OtpCheckResult checkResult = processOtpAttemptAndMaybeConsume(otpCode.getId(), verifyDto.getCode());
//        // ضمن verifyOtp(...) استبدل الاستدعاء الداخلي بالاستدعاء للـ service الجديد:
//        OtpProcessingService.OtpCheckResult checkResult =
//                otpProcessingService.processOtpAttemptAndMaybeConsume(otpCode.getId(), verifyDto.getCode());
//
//        switch (checkResult) {
//            case SUCCESS:
//                // ok -> proceed
//                break;
//            case INVALID:
//                throw new OtpValidationException("Invalid OTP code");
//            case TOO_MANY_ATTEMPTS:
//                throw new OtpValidationException("Too many attempts");
//            case EXPIRED:
//            case NOT_FOUND:
//            default:
//                throw new OtpValidationException("Invalid or expired OTP");
//        }
//
//        // find or create user (هذا داخل ترانزكشن عادي)
//        User user = userRepository.findByNormalizedPhone(normalized)
//                .orElseGet(() -> createOrGetUser(verifyDto.getPhone(), normalized));
//
//        if (!user.isVerified()) {
//            user.setVerified(true);
//            userRepository.save(user);
//        }
//
//        // إصدار التوكنات داخل TRANSACTION منفصل يقوم بحفظ refresh token و session
//        return issueNewTokensTransactional(user, verifyDto.getDeviceId(), verifyDto.getIp());
//    }
//
//
//    /**
//     * تجديد التوكنات - يقرأ الـ refresh token عن طريق jti ثم يتحقّق من المطابقة hash.
//     */
//    @Transactional
//    public AuthResponseDto refreshToken(RefreshRequestDto refreshDto) {
//        String raw = refreshDto.getRefreshToken();
//        String jtiStr;
//        try {
//            jtiStr = jwtUtil.extractJti(raw);
//        } catch (Exception ex) {
//            log.warn("refreshToken: invalid format: {}", ex.getMessage());
//            throw new TokenRefreshException("Invalid refresh token format");
//        }
//
//        UUID jti = UUID.fromString(jtiStr);
//
//        RefreshToken refreshToken = refreshTokenRepository.findByJti(jti)
//                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));
//
//        // تحقق أن الـ raw يطابق الـ hash المخزن
//        if (!passwordEncoder.matches(raw, refreshToken.getRefreshHash())) {
//            // كشف إعادة استخدام أو محاولة تزوير — ابطِل كل توكنات المستخدم
//            log.warn("refreshToken: hash mismatch for jti={}, revoking all for user {}", jti, refreshToken.getUser().getId());
//            refreshTokenRepository.revokeAllForUser(refreshToken.getUser().getId());
//            throw new TokenReuseException("Token mismatch / reuse detected");
//        }
//
//        if (refreshToken.getRevokedAt() != null || refreshToken.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
//            log.warn("refreshToken: token revoked or expired jti={}", jti);
//            refreshTokenRepository.revokeAllForUser(refreshToken.getUser().getId());
//            throw new TokenReuseException("Token is revoked or expired");
//        }
//
//        // Rotate tokens
//        return rotateTokens(refreshToken, raw);
//    }
//
//    /**
//     * تسجيل الخروج: ابطال الـ refresh token إذا كان يطابق المخزن.
//     */
//    @Transactional
//    public void logout(String refreshTokenRaw) {
//        String jtiStr = jwtUtil.extractJti(refreshTokenRaw);
//        UUID jti = UUID.fromString(jtiStr);
//
//        refreshTokenRepository.findByJti(jti).ifPresent(token -> {
//            if (passwordEncoder.matches(refreshTokenRaw, token.getRefreshHash())) {
//                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                refreshTokenRepository.save(token);
//                userSessionRepository.findByActiveJti(token.getJti()).ifPresent(session -> {
//                    session.setActiveJti(null);
//                    userSessionRepository.save(session);
//                });
//                log.debug("Logout: revoked refresh token jti={}", jti);
//            } else {
//                log.warn("Logout: token hash mismatch jti={}", jti);
//            }
//        });
//    }
//
//    @Transactional
//    public void revokeAllSessions(UUID userId) {
//        refreshTokenRepository.revokeAllForUser(userId);
//        userSessionRepository.deleteByUserId(userId);
//    }
//
//    @Transactional(readOnly = true)
//    public List<UserSession> getActiveSessions(UUID userId) {
//        return userSessionRepository.findByUserIdAndActiveTrue(userId);
//    }
//
//    @Transactional
//    public void revokeSession(UUID sessionId, UUID userId) {
//        userSessionRepository.findById(sessionId).ifPresent(session -> {
//            if (!session.getUser().getId().equals(userId)) {
//                throw new AuthorizationException("Cannot revoke another user's session");
//            }
//            session.revoke();
//            userSessionRepository.save(session);
//
//            refreshTokenRepository.findBySessionId(sessionId).ifPresent(token -> {
//                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                refreshTokenRepository.save(token);
//            });
//        });
//    }
//
//    // إصدار التوكنات وحفظ refresh token + session ضمن TRANSACTION منفصل
//    @Transactional
//    public AuthResponseDto issueNewTokensTransactional(User user, String deviceId, String ip) {
//        String accessToken = jwtUtil.generateAccessToken(user);
//        String refreshToken = jwtUtil.generateRefreshToken(user);
//        String refreshHash = passwordEncoder.encode(refreshToken);
//
//        RefreshToken newToken = saveRefreshToken(user, refreshToken, refreshHash, deviceId, ip);
//
//        enforceSingleActiveSession(user, newToken, deviceId, ip);
//
//        return new AuthResponseDto(
//                accessToken,
//                accessExpiration,
//                refreshToken,
//                refreshExpiration,
//                user.getId().toString()
//        );
//    }
//
//    private AuthResponseDto rotateTokens(RefreshToken oldToken, String rawOldRefresh) {
//        String newAccessToken = jwtUtil.generateAccessToken(oldToken.getUser());
//        String newRefreshToken = jwtUtil.generateRefreshToken(oldToken.getUser());
//        String newRefreshHash = passwordEncoder.encode(newRefreshToken);
//
//        RefreshToken newToken = saveRefreshToken(oldToken.getUser(), newRefreshToken, newRefreshHash, oldToken.getDeviceId(), oldToken.getIpAddress());
//
//        oldToken.setReplacedBy(newToken.getJti());
//        oldToken.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//        refreshTokenRepository.save(oldToken);
//
//        updateActiveSession(oldToken.getUser(), newToken);
//
//        return new AuthResponseDto(
//                newAccessToken,
//                accessExpiration,
//                newRefreshToken,
//                refreshExpiration,
//                oldToken.getUser().getId().toString()
//        );
//    }
//
//    private RefreshToken saveRefreshToken(User user, String rawRefreshToken, String hash, String deviceId, String ip) {
//        String jtiStr = jwtUtil.extractJti(rawRefreshToken);
//        RefreshToken token = RefreshToken.builder()
//                .jti(UUID.fromString(jtiStr))
//                .user(user)
//                .refreshHash(hash)
//                .deviceId(deviceId)
//                .ipAddress(ip)
//                .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
//                .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(refreshExpiration))
//                .build();
//
//        return refreshTokenRepository.save(token);
//    }
//
//    private void enforceSingleActiveSession(User user, RefreshToken token, String deviceId, String ip) {
//        // revoke all previous tokens for this user
//        refreshTokenRepository.revokeAllForUser(user.getId());
//
//        UserSession session = userSessionRepository.findByUserId(user.getId())
//                .orElse(new UserSession());
//
//        session.setUser(user);
//        session.setActiveJti(token.getJti());
//        session.setDeviceId(deviceId);
//        session.setIpAddress(ip);
//        session.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));
//        session.setExpiresAt(token.getExpiresAt());
//
//        userSessionRepository.save(session);
//    }
//
//    private void updateActiveSession(User user, RefreshToken token) {
//        userSessionRepository.findByUserId(user.getId()).ifPresent(session -> {
//            session.setActiveJti(token.getJti());
//            session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            userSessionRepository.save(session);
//        });
//    }
//
//    private User createNewUser(String phone, String normalizedPhone) {
//        User user = new User();
//        user.setPhoneNumber(phone);
//        user.setCountryCode(phone.substring(0, Math.min(phone.length(), 5)));
//        user.setVerified(true);
//        user.setAccountStatus(AccountStatus.ACTIVE);
//        return userRepository.save(user);
//    }
//    private User createOrGetUser(String phone, String normalizedPhone) {
//        // حاول العثور على المستخدم أولاً
//        Optional<User> existing = userRepository.findByNormalizedPhone(normalizedPhone);
//        if (existing.isPresent()) {
//            return existing.get();
//        }
//
//        // لم نجده: حاول الإنشاء، لكن التقط استثناء تكرار المفتاح (concurrent insert)
//        User user = new User();
//        user.setPhoneNumber(phone);
//        user.setCountryCode(phone.substring(0, Math.min(phone.length(), 5)));
//        user.setVerified(true);
//        user.setNormalizedPhone(normalizedPhone); // ← هنا لازم
//
//        user.setAccountStatus(AccountStatus.ACTIVE);
//
//        try {
//            return userRepository.save(user);
//        } catch (DataIntegrityViolationException ex) {
//            // من المرجّح أن شخصاً آخر أدخل نفس الهاتف في نفس الوقت — أعد القراءة
//            Optional<User> retry = userRepository.findByNormalizedPhone(normalizedPhone);
//            if (retry.isPresent()) {
//                return retry.get();
//            }
//            // إن لم نجد بعد المحاولة الثانية، أعِد رمي الاستثناء الأصلي للـ caller ليعالجه
//            throw ex;
//        }
//    }
//
//    // نُعرّف enum داخلي لنتائج فحص الـ OTP
//    private enum OtpCheckResult {
//        SUCCESS,
//        INVALID,
//        TOO_MANY_ATTEMPTS,
//        EXPIRED,
//        NOT_FOUND
//    }
//}
package com.nova.poneglyph.service.auth;

import com.nova.poneglyph.domain.auth.OtpCode;
import com.nova.poneglyph.domain.auth.RefreshToken;
import com.nova.poneglyph.domain.enums.AccountStatus;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;
import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.dto.authDto.AuthResponseDto;
import com.nova.poneglyph.dto.authDto.OtpRequestDto;
import com.nova.poneglyph.dto.authDto.OtpVerifyDto;
import com.nova.poneglyph.dto.authDto.RefreshRequestDto;
import com.nova.poneglyph.dto.userDto.UserSessionDto;
import com.nova.poneglyph.exception.*;
import com.nova.poneglyph.repository.*;
import com.nova.poneglyph.service.audit.AuditService;
import com.nova.poneglyph.util.EncryptionUtil; // ← سنستخدمه لـ SHA-256
import com.nova.poneglyph.util.JwtUtil;
import com.nova.poneglyph.util.OtpGenerator;
import com.nova.poneglyph.util.PhoneUtil;
import com.nova.poneglyph.util.RateLimiterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final AuditService auditService;

    private final PasswordEncoder passwordEncoder;
    private final EncryptionUtil encryptionUtil;
    private final JwtUtil jwtUtil;
    private final PhoneUtil phoneUtil;
    private final RateLimiterService rateLimiterService;
    private final OtpProcessingService otpProcessingService;
    private final UserDeviceRepository userDeviceRepository;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    @Value("${otp.expiration.minutes}")
    private int otpExpirationMinutes;

    @Value("${otp.max-attempts}")
    private int maxOtpAttempts;

    /* ===========================
       طلب OTP
       =========================== */
    @Transactional
    public void requestOtp(OtpRequestDto requestDto) {
        String normalized = PhoneUtil.normalizePhone(requestDto.getPhone());

        if (rateLimiterService.isRateLimited("otp_request:" + normalized, 8, Duration.ofMinutes(15))) {
            throw new RateLimitExceededException("Too many OTP requests");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = now.plusMinutes(otpExpirationMinutes);

        String otp = OtpGenerator.generate(6);
        String otpHash = passwordEncoder.encode(otp);

        OtpCode otpCode = OtpCode.builder()
                .phoneNumber(requestDto.getPhone())
                .normalizedPhone(normalized)
                .codeHash(otpHash)
                .expiresAt(expiresAt)
                .requesterIp(requestDto.getIp())
                .deviceFingerprint(requestDto.getDeviceFingerprint())
                .createdAt(now)
                .attempts(0)
                .used(false)
                .build();

        otpCodeRepository.save(otpCode);

        // TODO: إرسال الكود عبر SMS فعلياً
        log.debug("OTP (test only) for {} is {}", normalized, otp);

        // تسجيل حدث التدقيق
        auditService.logAuthEvent(null, "OTP_REQUEST", "SENT", requestDto.getIp());
    }

    /* ===========================
       تحقق OTP + إنشاء/جلب المستخدم
       =========================== */
    @Transactional
    public AuthResponseDto verifyOtp(OtpVerifyDto verifyDto) {
        String normalized = PhoneUtil.normalizePhone(verifyDto.getPhone());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        Optional<OtpCode> otpOptional = otpCodeRepository.findLatestValidOtp(normalized, now);
        if (otpOptional.isEmpty()) {
            auditService.logAuthEvent(null, "OTP_VERIFY", "FAILED_NO_OTP", verifyDto.getIp());
            throw new OtpValidationException("Invalid or expired OTP");
        }

        OtpProcessingService.OtpCheckResult checkResult =
                otpProcessingService.processOtpAttemptAndMaybeConsume(otpOptional.get().getId(), verifyDto.getCode());

        switch (checkResult) {
            case SUCCESS -> { /* proceed */ }
            case INVALID -> {
                auditService.logAuthEvent(null, "OTP_VERIFY", "FAILED_INVALID", verifyDto.getIp());
                throw new OtpValidationException("Invalid OTP code");
            }
            case TOO_MANY_ATTEMPTS, EXPIRED, NOT_FOUND -> {
                auditService.logAuthEvent(null, "OTP_VERIFY", "FAILED_" + checkResult.name(), verifyDto.getIp());
                throw new OtpValidationException("Invalid or expired OTP");
            }
        }

        User user = userRepository.findByNormalizedPhone(normalized)
                .orElseGet(() -> createUserIfNotExists(verifyDto.getPhone()));

        if (!user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);
        }
        // التحقق مما إذا كان المستخدم لديه جلسة نشطة بالفعل
        List<UserSession> activeSessions = userSessionRepository.findActiveSessionsByUserId(user.getId());
        if (!activeSessions.isEmpty()) {
            // إذا كان لديه جلسة نشطة، نلغيها قبل إنشاء جلسة جديدة
            revokeAllActiveSessionsAndTokens(user.getId());
        }

        AuthResponseDto response = issueNewTokensTransactional(user, verifyDto.getDeviceId(), verifyDto.getIp());

        // تسجيل حدث التدقيق الناجح
        auditService.logAuthEvent(user.getId(), "OTP_VERIFY", "SUCCESS", verifyDto.getIp());

        return response;
    }

    /* ===========================
       تجديد التوكنات مع التحسينات الأمنية
       =========================== */
    @Transactional
    public AuthResponseDto refreshToken(RefreshRequestDto refreshDto) {
        String raw = refreshDto.getRefreshToken();
        final String jtiStr;

        try {
            jtiStr = jwtUtil.extractJti(raw);
        } catch (Exception ex) {
            log.warn("refreshToken: invalid token format: {}", ex.getMessage());
            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_INVALID_FORMAT");
            throw new TokenRefreshException("Invalid refresh token format");
        }

        UUID jti = UUID.fromString(jtiStr);

        // التحقق من rate limiting أولاً
        String rateLimitKey = "refresh_" + jtiStr;
        if (rateLimiterService.isRateLimited(rateLimitKey, 5, Duration.ofMinutes(1))) {
            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "RATE_LIMITED");
            throw new RateLimitExceededException("Too many refresh requests");
        }

        RefreshToken dbToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in DB: jti={}", jti);
                    auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_TOKEN_NOT_FOUND");
                    throw new TokenRefreshException("Invalid refresh token");
                });

        // التحقق من صلاحية الـ refresh token في JWT
        if (!jwtUtil.isRefreshTokenValid(raw, dbToken.getUser().getId().toString())) {
            log.warn("Invalid refresh token JWT: jti={}", jti);
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_INVALID_JWT");
            throw new TokenRefreshException("Invalid refresh token");
        }

        // التحقق من أن الـ refresh token غير منتهي ولم يتم إلغاؤه
        if (dbToken.getRevokedAt() != null) {
            log.warn("Refresh token already revoked: jti={}", jti);
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_ALREADY_REVOKED");
            throw new TokenRefreshException("Refresh token revoked");
        }

        if (dbToken.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            log.warn("Refresh token expired: jti={}", jti);
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_EXPIRED");
            throw new TokenRefreshException("Refresh token expired");
        }

        // مقارنة SHA-256
        String candidateHash = encryptionUtil.hash(raw);
        if (!candidateHash.equals(dbToken.getRefreshHash())) {
            log.warn("refreshToken hash mismatch jti={}, user={}", jti, dbToken.getUser().getId());
            revokeAllTokensForUser(dbToken.getUser().getId(), "Token hash mismatch detected");
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REUSE_DETECTED", "All tokens revoked");
            throw new TokenReuseException("Token mismatch / reuse detected");
        }

        AuthResponseDto response = rotateTokens(dbToken, raw);

        // تسجيل حدث التدقيق الناجح
//        auditService.logAuthEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "SUCCESS", null);

        return response;
    }

    /* ===========================
       تسجيل الخروج
       =========================== */
    @Transactional
    public void logout(String refreshTokenRaw) {
        try {
            String jtiStr = jwtUtil.extractJti(refreshTokenRaw);
            UUID jti = UUID.fromString(jtiStr);

            refreshTokenRepository.findByJti(jti).ifPresent(token -> {
                String candidateHash = encryptionUtil.hash(refreshTokenRaw);
                if (candidateHash.equals(token.getRefreshHash())) {
                    token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    refreshTokenRepository.save(token);

                    userSessionRepository.findByActiveJti(token.getJti()).ifPresent(session -> {
                        session.setActiveJti(null);
                        userSessionRepository.save(session);
                    });

                    // تسجيل حدث التدقيق
//                    auditService.logAuthEvent(token.getUser().getId(), "LOGOUT", "SUCCESS", null);
                    log.debug("Logout ok: revoked jti={}", jti);
                } else {
                    log.warn("Logout: token hash mismatch jti={}", jti);
                    auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "FAILED_HASH_MISMATCH");
                }
            });
        } catch (Exception ex) {
            log.error("Logout error: {}", ex.getMessage());
            auditService.logSecurityEvent(null, "LOGOUT", "FAILED_EXCEPTION: " + ex.getMessage());
            throw new TokenRefreshException("Logout failed");
        }
    }

    /* ===========================
       إلغاء جميع الجلسات
       =========================== */
    @Transactional
    public void revokeAllSessions(UUID userId) {
        // أولاً: إلغاء جميع توكنات التحديث للمستخدم
        refreshTokenRepository.revokeAllForUser(userId);

        // ثانياً: إلغاء جميع الجلسات (بدلاً من حذفها)
        userSessionRepository.findByUserId(userId).forEach(session -> {
            session.setActive(false);
            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userSessionRepository.save(session);
        });

        // تسجيل حدث التدقيق
        auditService.logSecurityEvent(userId, "REVOKE_ALL_SESSIONS", "All sessions and tokens revoked");
    }

    /* ===========================
       الحصول على الجلسات النشطة
       =========================== */
//    @Transactional(readOnly = true)
//    public List<UserSession> getActiveSessions(UUID userId) {
//        return userSessionRepository.findByUserIdAndActiveTrue(userId);
//    }
        @Transactional(readOnly = true)
    public List<UserSessionDto> getActiveSessions(UUID userId) {
        return userSessionRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(s -> new UserSessionDto(
                        s.getId(),
                        s.getDeviceId(),
                        s.getDevice() != null ? s.getDevice().getDeviceName() : null,
                        s.getIssuedAt(),
                        s.getLastUsedAt(),
                        s.getExpiresAt(),
                        s.isActive(),
                        s.getIpAddress(),
                        s.getUserAgent(),
                        s.isOnline(),
                        s.getActiveJti()
                ))
                .toList();
    }
//

    /* ===========================
       إلغاء جلسة محددة
       =========================== */
    @Transactional
    public void revokeSession(UUID sessionId, UUID userId) {
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            if (!session.getUser().getId().equals(userId)) {
                auditService.logSecurityEvent(userId, "REVOKE_SESSION", "FAILED_UNAUTHORIZED");
                throw new AuthorizationException("Cannot revoke another user's session");
            }
            session.revoke();
            userSessionRepository.save(session);

            refreshTokenRepository.findBySessionId(sessionId).ifPresent(token -> {
                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
                refreshTokenRepository.save(token);
            });

            // تسجيل حدث التدقيق
            auditService.logSecurityEvent(userId, "REVOKE_SESSION", "Session revoked: " + sessionId);
        });
    }

    /* ===========================
       إلغاء جميع التوكنات للمستخدم (للاكتشافات الأمنية)
       =========================== */
    @Transactional
    public void revokeAllTokensForUser(UUID userId, String reason) {
        log.warn("Revoking all tokens for user {}: {}", userId, reason);
        refreshTokenRepository.revokeAllForUser(userId);

        // إلغاء جميع الجلسات النشطة
        userSessionRepository.findByUserIdAndActiveTrue(userId).forEach(session -> {
            session.setActive(false);
            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
            userSessionRepository.save(session);
        });

        // تسجيل حدث أمني
        auditService.logSecurityEvent(userId, "TOKENS_REVOKED",
                "All tokens revoked due to: " + reason);
    }

    /* ===========================
       إصدار التوكنات (جلسة واحدة فعّالة)
       =========================== */
//    @Transactional
//    public AuthResponseDto issueNewTokensTransactional(User user, String deviceId, String ip) {
//        String accessToken = jwtUtil.generateAccessToken(user);
//        String refreshToken = jwtUtil.generateRefreshToken(user);
//
//        // استخرج jti أولاً، ثم ألغِ كل القديم ما عدا الـ jti الجديد قبل الحفظ
//        String jtiStr = jwtUtil.extractJti(refreshToken);
//        UUID jti = UUID.fromString(jtiStr);
//        refreshTokenRepository.revokeAllForUserExcept(user.getId(), jti);
//
//        // خزّن الـ refresh كـ SHA-256
//        String refreshHash = encryptionUtil.hash(refreshToken);
//
//        RefreshToken newToken = saveRefreshToken(user, jti, refreshHash, deviceId, ip);
//
//        upsertSingleActiveSession(user, newToken, deviceId, ip);
//
//        return new AuthResponseDto(
//                accessToken,
//                accessExpiration,
//                refreshToken,
//                refreshExpiration,
//                user.getId().toString()
//        );
//    }

    @Transactional
    public AuthResponseDto issueNewTokensTransactional(User user, String deviceId, String ip) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // استخرج jti أولاً، ثم ألغِ كل القديم ما عدا الـ jti الجديد قبل الحفظ
        String jtiStr = jwtUtil.extractJti(refreshToken);
        UUID jti = UUID.fromString(jtiStr);
        refreshTokenRepository.revokeAllForUserExcept(user.getId(), jti);

        // خزّن الـ refresh كـ SHA-256
        String refreshHash = encryptionUtil.hash(refreshToken);

        // إنشاء أو تحديث جهاز المستخدم
        UserDevice userDevice = findOrCreateUserDevice(user, deviceId, ip);

        // إنشاء التوكن والجلسة
        RefreshToken newToken = saveRefreshToken(user, jti, refreshHash, userDevice, ip);
        // التأكد من عدم وجود أكثر من جلسة نشطة
        List<UserSession> activeSessions = userSessionRepository.findActiveSessionsByUserId(user.getId());
        if (activeSessions.size() > 1) {
            log.warn("User {} has multiple active sessions. Revoking all except the latest.", user.getId());

            // الاحتفاظ بأحدث جلسة فقط
            UserSession latestSession = activeSessions.stream()
                    .max(Comparator.comparing(UserSession::getLastUsedAt))
                    .orElse(null);

            // إلغاء جميع الجلسات الأخرى
            for (UserSession session : activeSessions) {
                if (!session.equals(latestSession)) {
                    session.setActive(false);
                    session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    userSessionRepository.save(session);
                }
            }
        }

        upsertSingleActiveSession(user, newToken, userDevice, ip);

        return new AuthResponseDto(
                accessToken,
                accessExpiration,
                refreshToken,
                refreshExpiration,
                user.getId().toString()
        );
    }

    private UserDevice findOrCreateUserDevice(User user, String deviceId, String ip) {
        return userDeviceRepository.findByUserAndDeviceId(user, deviceId)
                .orElseGet(() -> {
                    UserDevice newDevice = UserDevice.builder()
                            .user(user)
                            .deviceId(deviceId)
                            .ipAddress(ip)
                            .lastLogin(OffsetDateTime.now(ZoneOffset.UTC))
                            .active(true)
                            .build();
                    return userDeviceRepository.save(newDevice);
                });
    }

//    private RefreshToken saveRefreshToken(User user, UUID jti, String hash, UserDevice device, String ip) {
//        RefreshToken token = RefreshToken.builder()
//                .jti(jti)
//                .user(user)
//                .refreshHash(hash)
//                .device(device) // استخدام العلاقة مع الجهاز
//                .ipAddress(ip)
//                .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
//                .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(refreshExpiration))
//                .build();
//        return refreshTokenRepository.save(token);
//    }

    private void upsertSingleActiveSession(User user, RefreshToken token, UserDevice device, String ip) {
        UserSession session = userSessionRepository.findByUserAndDevice(user, device)
                .orElse(new UserSession());

        session.setUser(user);
        session.setDevice(device); // استخدام العلاقة بدلاً من تخزين deviceId منفصل
        session.setActiveJti(token.getJti());
        session.setIpAddress(ip);
        session.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));
        session.setExpiresAt(token.getExpiresAt());
        session.setRefreshTokenHash(token.getRefreshHash());

        userSessionRepository.save(session);
    }


//    @Transactional
//    public AuthResponseDto issueNewTokensTransactional(User user, String deviceId, String ip) {
//        String accessToken = jwtUtil.generateAccessToken(user);
//        String refreshToken = jwtUtil.generateRefreshToken(user);
//
//        // استخرج jti أولاً
//        String jtiStr = jwtUtil.extractJti(refreshToken);
//        UUID jti = UUID.fromString(jtiStr);
//
//        // إلغاء جميع الجلسات والتوكنات النشطة السابقة
//        revokeAllActiveSessionsAndTokens(user.getId());
//
//        // خزّن الـ refresh كـ SHA-256
//        String refreshHash = encryptionUtil.hash(refreshToken);
//
//        RefreshToken newToken = saveRefreshToken(user, jti, refreshHash, deviceId, ip);
//        UserSession session = createOrUpdateSession(user, newToken, deviceId, ip);
//
//        return new AuthResponseDto(
//                accessToken,
//                accessExpiration,
//                refreshToken,
//                refreshExpiration,
//                user.getId().toString()
//        );
//    }

    @Transactional
    public void revokeAllActiveSessionsAndTokens(UUID userId) {
        // إلغاء جميع التوكنات النشطة
        refreshTokenRepository.revokeAllForUser(userId);

        // إلغاء جميع الجلسات النشطة
        userSessionRepository.revokeAllActiveSessionsForUser(userId);
    }

//    private UserSession createOrUpdateSession(User user, RefreshToken token, String deviceId, String ip) {
//        // البحث عن جلسة نشطة موجودة لهذا المستخدم
//        Optional<UserSession> existingSession = userSessionRepository.findByUserId(user.getId())
//                .filter(UserSession::isActive);
//
//        UserSession session;
//
//        if (existingSession.isPresent()) {
//            // تحديث الجلسة الموجودة
//            session = existingSession.get();
//            session.setActiveJti(token.getJti());
//            session.setDeviceId(deviceId);
//            session.setIpAddress(ip);
//            session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            session.setExpiresAt(token.getExpiresAt());
//            session.setRefreshTokenHash(token.getRefreshHash());
//        } else {
//            // إنشاء جلسة جديدة
//            session = UserSession.builder()
//                    .user(user)
//                    .activeJti(token.getJti())
//                    .deviceId(deviceId)
//                    .ipAddress(ip)
//                    .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
//                    .lastUsedAt(OffsetDateTime.now(ZoneOffset.UTC))
//                    .expiresAt(token.getExpiresAt())
//                    .refreshTokenHash(token.getRefreshHash())
//                    .active(true)
//                    .build();
//        }
//
//        return userSessionRepository.save(session);
//    }
    /* ===========================
       تدوير التوكنات (لعملية التجديد)
       =========================== */
//    private AuthResponseDto rotateTokens(RefreshToken oldToken, String rawOldRefresh) {
//        String newAccessToken = jwtUtil.generateAccessToken(oldToken.getUser());
//        String newRefreshToken = jwtUtil.generateRefreshToken(oldToken.getUser());
//
//        String newJtiStr = jwtUtil.extractJti(newRefreshToken);
//        UUID newJti = UUID.fromString(newJtiStr);
//
//        // ألغِ كل القديم ما عدا الجديد
//        refreshTokenRepository.revokeAllForUserExcept(oldToken.getUser().getId(), newJti);
//
//        String newRefreshHash = encryptionUtil.hash(newRefreshToken);
//
//        RefreshToken newToken = saveRefreshToken(oldToken.getUser(), newJti, newRefreshHash,
//                oldToken.getDeviceId(), oldToken.getIpAddress());
//
//        // علّم القديم بأنه استُبدل
//        oldToken.setReplacedBy(newToken.getJti());
//        oldToken.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//        refreshTokenRepository.save(oldToken);
//
//        updateActiveSession(oldToken.getUser(), newToken);
//
//        return new AuthResponseDto(
//                newAccessToken,
//                accessExpiration,
//                newRefreshToken,
//                refreshExpiration,
//                oldToken.getUser().getId().toString()
//        );
//    }
private AuthResponseDto rotateTokens(RefreshToken oldToken, String rawOldRefresh) {
    String newAccessToken = jwtUtil.generateAccessToken(oldToken.getUser());
    String newRefreshToken = jwtUtil.generateRefreshToken(oldToken.getUser());

    String newJtiStr = jwtUtil.extractJti(newRefreshToken);
    UUID newJti = UUID.fromString(newJtiStr);

    // إلغاء جميع التوكنات القديمة ما عدا الجديد
    refreshTokenRepository.revokeAllForUserExcept(oldToken.getUser().getId(), newJti);

    String newRefreshHash = encryptionUtil.hash(newRefreshToken);

    // استخدام نفس الجهاز من التوكن القديم
    UserDevice device = oldToken.getDevice();

    RefreshToken newToken = saveRefreshToken(oldToken.getUser(), newJti, newRefreshHash,
            device, oldToken.getIpAddress());

    // تحديث الجلسة النشطة بالـ jti الجديد
//    updateActiveSession(oldToken.getUser(), newToken);

    // ربط التوكن الجديد بالجلسة النشطة
    // استخدام الدالة المعدلة
    updateActiveSession(oldToken.getUser(), newToken);

    // ربط التوكن الجديد بالجلسة النشطة
    Optional<UserSession> activeSession = userSessionRepository.findLatestActiveSessionByUserId(oldToken.getUser().getId());

    if (activeSession.isPresent()) {
        newToken.setSession(activeSession.get());
        refreshTokenRepository.save(newToken);
    }

    // علّم القديم بأنه استُبدل
    oldToken.setReplacedBy(newToken.getJti());
    oldToken.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
    refreshTokenRepository.save(oldToken);

    return new AuthResponseDto(
            newAccessToken,
            accessExpiration,
            newRefreshToken,
            refreshExpiration,
            oldToken.getUser().getId().toString()
    );
}
private RefreshToken saveRefreshToken(User user, UUID jti, String hash, UserDevice device, String ip) {
        RefreshToken token = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .refreshHash(hash)
                .device(device) // استخدام العلاقة مع الجهاز
                .ipAddress(ip)
                .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(refreshExpiration))
                .build();
        return refreshTokenRepository.save(token);
    }
    /* ===========================
       حفظ refresh token
       =========================== */
//    private RefreshToken saveRefreshToken(User user, UUID jti, String hash, String deviceId, String ip) {
//        RefreshToken token = RefreshToken.builder()
//                .jti(jti)
//                .user(user)
//                .refreshHash(hash)
//                .deviceId(deviceId)
//                .ipAddress(ip)
//                .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
//                .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(refreshExpiration))
//                .build();
//        return refreshTokenRepository.save(token);
//    }

    /* ===========================
       إنشاء/تحديث جلسة مستخدم
       =========================== */
//    private void upsertSingleActiveSession(User user, RefreshToken token, String deviceId, String ip) {
//        UserSession session = userSessionRepository.findByUserId(user.getId())
//                .orElse(new UserSession());
//
//        session.setUser(user);
//        session.setActiveJti(token.getJti());
//        session.setDeviceId(deviceId);
//        session.setIpAddress(ip);
//        session.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));
//        session.setExpiresAt(token.getExpiresAt());
//        session.setRefreshTokenHash(token.getRefreshHash());
//
//        userSessionRepository.save(session);
//    }
    private void updateActiveSession(User user, RefreshToken token) {
        // استخدام الدالة الجديدة التي ترجع جلسة واحدة فقط
        userSessionRepository.findLatestActiveSessionByUserId(user.getId())
                .ifPresent(session -> {
                    session.setActiveJti(token.getJti());
                    session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    session.setRefreshTokenHash(token.getRefreshHash());
                    session.setExpiresAt(token.getExpiresAt());

                    // تحديث العلاقة مع الجهاز من التوكن
                    if (token.getDevice() != null) {
                        session.setDevice(token.getDevice());
                    }

                    userSessionRepository.save(session);
                });
    }//    private void updateActiveSession(User user, RefreshToken token) {
//        userSessionRepository.findByUserId(user.getId()).ifPresent(session -> {
//            session.setActiveJti(token.getJti());
//            session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            session.setRefreshTokenHash(token.getRefreshHash());
//            userSessionRepository.save(session);
//        });
//    }

    /* ===========================
       إنشاء مستخدم جديد
       =========================== */
    private User createUserIfNotExists(String phone) {
        User user = new User();
        user.setPhoneNumber(phone);
        user.setCountryCode(phone.substring(0, Math.min(phone.length(), 5)));
        user.setVerified(true);
        user.setNormalizedPhone(PhoneUtil.normalizePhone(phone));
        user.setAccountStatus(AccountStatus.ACTIVE);
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // احتمال تسابق: لو انحفظ مستخدم بنفس الهاتف قبلنا مباشرة
            return userRepository.findByNormalizedPhone(PhoneUtil.normalizePhone(phone))
                    .orElseThrow(() -> ex);
        }
    }
}

//
//@Log4j2
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final OtpCodeRepository otpCodeRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final UserSessionRepository userSessionRepository;
//
//    private final PasswordEncoder passwordEncoder; // لِـ OTP فقط
//    private final EncryptionUtil encryptionUtil;   // لِـ SHA-256 للريفريش
//
//    private final JwtUtil jwtUtil;
//    private final PhoneUtil phoneUtil;
//    private final RateLimiterService rateLimiterService;
//    private final OtpProcessingService otpProcessingService;
//    private final AuditService auditService;
//
//    @Value("${jwt.access.expiration}")
//    private long accessExpiration;
//
//    @Value("${jwt.refresh.expiration}")
//    private long refreshExpiration;
//
//    @Value("${otp.expiration.minutes}")
//    private int otpExpirationMinutes;
//
//    @Value("${otp.max-attempts}")
//    private int maxOtpAttempts;
//
//    /* ===========================
//       طلب OTP
//       =========================== */
//    @Transactional
//    public void requestOtp(OtpRequestDto requestDto) {
//        String normalized = PhoneUtil.normalizePhone(requestDto.getPhone());
//
//        if (rateLimiterService.isRateLimited("otp_request:" + normalized, 8, Duration.ofMinutes(15))) {
//            throw new RateLimitExceededException("Too many OTP requests");
//        }
//
//        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
//        OffsetDateTime expiresAt = now.plusMinutes(otpExpirationMinutes);
//
//        String otp = OtpGenerator.generate(6);
//        String otpHash = passwordEncoder.encode(otp); // BCrypt للـ OTP
//
//        OtpCode otpCode = OtpCode.builder()
//                .phoneNumber(requestDto.getPhone())
//                .normalizedPhone(normalized)
//                .codeHash(otpHash)
//                .expiresAt(expiresAt)
//                .requesterIp(requestDto.getIp())
//                .deviceFingerprint(requestDto.getDeviceFingerprint())
//                .createdAt(now)
//                .attempts(0)
//                .used(false)
//                .build();
//
//        otpCodeRepository.save(otpCode);
//
//        // TODO: إرسال الكود عبر SMS فعلياً (لا تطبع الكود في الإنتاج)
//        log.debug("OTP (test only) for {} is {}", normalized, otp);
//    }
//
//    /* ===========================
//       تحقق OTP + إنشاء/جلب المستخدم
//       =========================== */
//    @Transactional // Add this annotation
//    public AuthResponseDto verifyOtp(OtpVerifyDto verifyDto) {
//        String normalized = PhoneUtil.normalizePhone(verifyDto.getPhone());
//        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
//
//        Optional<OtpCode> otpOptional = otpCodeRepository.findLatestValidOtp(normalized, now);
//        if (otpOptional.isEmpty()) {
//            throw new OtpValidationException("Invalid or expired OTP");
//        }
//
//        OtpProcessingService.OtpCheckResult checkResult =
//                otpProcessingService.processOtpAttemptAndMaybeConsume(otpOptional.get().getId(), verifyDto.getCode());
//
//        switch (checkResult) {
//            case SUCCESS -> { /* proceed */ }
//            case INVALID -> throw new OtpValidationException("Invalid OTP code");
//            case TOO_MANY_ATTEMPTS, EXPIRED, NOT_FOUND -> throw new OtpValidationException("Invalid or expired OTP");
//        }
//
//        // المستخدم: نعتمد أن normalized_phone عمود مولَّد DB (insertable/updatable=false)
//        User user = userRepository.findByNormalizedPhone(normalized)
//                .orElseGet(() -> createUserIfNotExists(verifyDto.getPhone()));
//
//        if (!user.isVerified()) {
//            user.setVerified(true);
//            userRepository.save(user);
//        }
//
//        return issueNewTokensTransactional(user, verifyDto.getDeviceId(), verifyDto.getIp());
//    }
//
//    /* ===========================
//       تجديد التوكنات
//       =========================== */
////    @Transactional
////    public AuthResponseDto refreshToken(RefreshRequestDto refreshDto) {
////        String raw = refreshDto.getRefreshToken();
////        final String jtiStr;
////        try {
////            jtiStr = jwtUtil.extractJti(raw);
////        } catch (Exception ex) {
////            log.warn("refreshToken: invalid token format: {}", ex.getMessage());
////            throw new TokenRefreshException("Invalid refresh token format");
////        }
////        UUID jti = UUID.fromString(jtiStr);
////
////        RefreshToken dbToken = refreshTokenRepository.findByJti(jti)
////                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));
////
////        // مقارنة SHA-256 (بدون BCrypt)
////        String candidateHash = encryptionUtil.hash(raw);
////        if (!candidateHash.equals(dbToken.getRefreshHash())) {
////            log.warn("refreshToken mismatch jti={}, user={}", jti, dbToken.getUser().getId());
////            refreshTokenRepository.revokeAllForUser(dbToken.getUser().getId());
////            throw new TokenReuseException("Token mismatch / reuse detected");
////        }
////
////        if (dbToken.getRevokedAt() != null ||
////                dbToken.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
////            refreshTokenRepository.revokeAllForUser(dbToken.getUser().getId());
////            throw new TokenReuseException("Token is revoked or expired");
////        }
////
////        return rotateTokens(dbToken, raw);
////    }
//    @Transactional
//    public AuthResponseDto refreshToken(RefreshRequestDto refreshDto) {
//        String raw = refreshDto.getRefreshToken();
//        final String jtiStr;
//        try {
//            jtiStr = jwtUtil.extractJti(raw);
//        } catch (Exception ex) {
//            log.warn("refreshToken: invalid token format: {}", ex.getMessage());
//            throw new TokenRefreshException("Invalid refresh token format");
//        }
//        UUID jti = UUID.fromString(jtiStr);
//
//        // التحقق من rate limiting أولاً
//        String rateLimitKey = "refresh_" + jtiStr;
//        if (rateLimiterService.isRateLimited(rateLimitKey, 5, Duration.ofMinutes(1))) {
//            throw new RateLimitExceededException("Too many refresh requests");
//        }
//
//        RefreshToken dbToken = refreshTokenRepository.findByJti(jti)
//                .orElseThrow(() -> {
//                    log.warn("Refresh token not found in DB: jti={}", jti);
//                    throw new TokenRefreshException("Invalid refresh token");
//                });
//
//        // التحقق من صلاحية الـ refresh token في JWT
//        if (!jwtUtil.isRefreshTokenValid(raw, dbToken.getUser().getId().toString())) {
//            log.warn("Invalid refresh token JWT: jti={}", jti);
//            throw new TokenRefreshException("Invalid refresh token");
//        }
//
//        // التحقق من أن الـ refresh token غير منتهي ولم يتم إلغاؤه
//        if (dbToken.getRevokedAt() != null) {
//            log.warn("Refresh token already revoked: jti={}", jti);
//            throw new TokenRefreshException("Refresh token revoked");
//        }
//
//        if (dbToken.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
//            log.warn("Refresh token expired: jti={}", jti);
//            throw new TokenRefreshException("Refresh token expired");
//        }
//
//        // مقارنة SHA-256
//        String candidateHash = encryptionUtil.hash(raw);
//        if (!candidateHash.equals(dbToken.getRefreshHash())) {
//            log.warn("refreshToken hash mismatch jti={}, user={}", jti, dbToken.getUser().getId());
//            revokeAllTokensForUser(dbToken.getUser().getId(), "Token hash mismatch detected");
//            throw new TokenReuseException("Token mismatch / reuse detected");
//        }
//
//        return rotateTokens(dbToken, raw);
//    }
//
//    @Transactional
//    public void revokeAllTokensForUser(UUID userId, String reason) {
//        log.warn("Revoking all tokens for user {}: {}", userId, reason);
//        refreshTokenRepository.revokeAllForUser(userId);
//
//        // إلغاء جميع الجلسات النشطة
//        userSessionRepository.findByUserIdAndActiveTrue(userId).forEach(session -> {
//            session.setActive(false);
//            session.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            userSessionRepository.save(session);
//        });
//
//        // تسجيل حدث أمني
//        auditService.logSecurityEvent(userId, "TOKENS_REVOKED",
//                "All tokens revoked due to: " + reason);
//    }
//    /* ===========================
//       تسجيل الخروج
//       =========================== */
//    @Transactional
//    public void logout(String refreshTokenRaw) {
//        String jtiStr = jwtUtil.extractJti(refreshTokenRaw);
//        UUID jti = UUID.fromString(jtiStr);
//
//        refreshTokenRepository.findByJti(jti).ifPresent(token -> {
//            String candidateHash = encryptionUtil.hash(refreshTokenRaw);
//            if (candidateHash.equals(token.getRefreshHash())) {
//                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                refreshTokenRepository.save(token);
//
//                userSessionRepository.findByActiveJti(token.getJti()).ifPresent(session -> {
//                    session.setActiveJti(null);
//                    userSessionRepository.save(session);
//                });
//                log.debug("Logout ok: revoked jti={}", jti);
//            } else {
//                log.warn("Logout: token hash mismatch jti={}", jti);
//            }
//        });
//    }
//
//    @Transactional
//    public void revokeAllSessions(UUID userId) {
//        refreshTokenRepository.revokeAllForUser(userId);
//        userSessionRepository.deleteByUserId(userId);
//    }
//
////    @Transactional(readOnly = true)
////    public List<UserSession> getActiveSessions(UUID userId) {
////        return userSessionRepository.findByUserIdAndActiveTrue(userId);
////    }
//
//    @Transactional(readOnly = true)
//    public List<UserSessionDto> getActiveSessions(UUID userId) {
//        return userSessionRepository.findByUserIdAndActiveTrue(userId).stream()
//                .map(s -> new UserSessionDto(
//                        s.getId(),
//                        s.getDeviceId(),
//                        s.getDevice() != null ? s.getDevice().getDeviceName() : null,
//                        s.getIssuedAt(),
//                        s.getLastUsedAt(),
//                        s.getExpiresAt(),
//                        s.isActive(),
//                        s.getIpAddress(),
//                        s.getUserAgent(),
//                        s.isOnline(),
//                        s.getActiveJti()
//                ))
//                .toList();
//    }
//
//    @Transactional
//    public void revokeSession(UUID sessionId, UUID userId) {
//        userSessionRepository.findById(sessionId).ifPresent(session -> {
//            if (!session.getUser().getId().equals(userId)) {
//                throw new AuthorizationException("Cannot revoke another user's session");
//            }
//            session.revoke();
//            userSessionRepository.save(session);
//
//            refreshTokenRepository.findBySessionId(sessionId).ifPresent(token -> {
//                token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                refreshTokenRepository.save(token);
//            });
//        });
//    }
//
//    /* ===========================
//       إصدار التوكنات (جلسة واحدة فعّالة)
//       =========================== */
//
//
//
////    @Transactional
//    public AuthResponseDto issueNewTokensTransactional(User user, String deviceId, String ip) {
//        String accessToken = jwtUtil.generateAccessToken(user);
//        String refreshToken = jwtUtil.generateRefreshToken(user);
//
//        // استخرج jti أولاً، ثم ألغِ كل القديم ما عدا الـ jti الجديد قبل الحفظ
//        String jtiStr = jwtUtil.extractJti(refreshToken);
//        UUID jti = UUID.fromString(jtiStr);
//        refreshTokenRepository.revokeAllForUserExcept(user.getId(), jti);
//
//        // خزّن الـ refresh كـ SHA-256
//        String refreshHash = encryptionUtil.hash(refreshToken);
//
//        RefreshToken newToken = saveRefreshToken(user, jti, refreshHash, deviceId, ip);
//
//        upsertSingleActiveSession(user, newToken, deviceId, ip);
//
//        return new AuthResponseDto(
//                accessToken,
//                accessExpiration,
//                refreshToken,
//                refreshExpiration,
//                user.getId().toString()
//        );
//    }
//
//    private AuthResponseDto rotateTokens(RefreshToken oldToken, String rawOldRefresh) {
//        String newAccessToken = jwtUtil.generateAccessToken(oldToken.getUser());
//        String newRefreshToken = jwtUtil.generateRefreshToken(oldToken.getUser());
//
//        String newJtiStr = jwtUtil.extractJti(newRefreshToken);
//        UUID newJti = UUID.fromString(newJtiStr);
//
//        // التحقق من أن الجهاز لم يتغير (إذا كان deviceId متوفراً)
//        String currentDeviceId = oldToken.getDeviceId();
//        // احصل على deviceId من الطلب الحالي إذا كان متوفراً
//        if (currentDeviceId != null && !currentDeviceId.equals(oldToken.getDeviceId())) {
//            log.warn("Device mismatch during token refresh: old={}, new={}",
//                    oldToken.getDeviceId(), currentDeviceId);
//            revokeAllTokensForUser(oldToken.getUser().getId(), "Device mismatch during refresh");
//            throw new TokenRefreshException("Device mismatch detected");
//        }
//
//        // ألغِ كل القديم ما عدا الجديد
//        refreshTokenRepository.revokeAllForUserExcept(oldToken.getUser().getId(), newJti);
//
//        String newRefreshHash = encryptionUtil.hash(newRefreshToken);
//
//        RefreshToken newToken = saveRefreshToken(oldToken.getUser(), newJti, newRefreshHash, oldToken.getDeviceId(), oldToken.getIpAddress());
//
//        // علّم القديم بأنه استُبدل
//        oldToken.setReplacedBy(newToken.getJti());
//        oldToken.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//        refreshTokenRepository.save(oldToken);
//
//        updateActiveSession(oldToken.getUser(), newToken);
//
//        return new AuthResponseDto(
//                newAccessToken,
//                accessExpiration,
//                newRefreshToken,
//                refreshExpiration,
//                oldToken.getUser().getId().toString()
//        );
//    }
//
//    private RefreshToken saveRefreshToken(User user, UUID jti, String hash, String deviceId, String ip) {
//        RefreshToken token = RefreshToken.builder()
//                .jti(jti)
//                .user(user)
//                .refreshHash(hash)
//                .deviceId(deviceId)
//                .ipAddress(ip)
//                .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
//                .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(refreshExpiration))
//                .build();
//        return refreshTokenRepository.save(token);
//    }
//
////    private void upsertSingleActiveSession(User user, RefreshToken token, String deviceId, String ip) {
////        UserSession session = userSessionRepository.findByUserId(user.getId())
////                .orElse(new UserSession());
////
////        session.setUser(user);
////        session.setActiveJti(token.getJti());
////        session.setDeviceId(deviceId);
////        session.setIpAddress(ip);
////        session.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));
////        session.setExpiresAt(token.getExpiresAt());
////
////        userSessionRepository.save(session);
////    }
////
////    private void updateActiveSession(User user, RefreshToken token) {
////        userSessionRepository.findByUserId(user.getId()).ifPresent(session -> {
////            session.setActiveJti(token.getJti());
////            session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
////            userSessionRepository.save(session);
////        });
////    }
//    private void upsertSingleActiveSession(User user, RefreshToken token, String deviceId, String ip) {
//        UserSession session = userSessionRepository.findByUserId(user.getId())
//                .orElse(new UserSession());
//
//        session.setUser(user);
//        session.setActiveJti(token.getJti());
//        session.setDeviceId(deviceId);
//        session.setIpAddress(ip);
//        session.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));
//        session.setExpiresAt(token.getExpiresAt());
//        session.setRefreshTokenHash(token.getRefreshHash()); // Add this line
//
//        userSessionRepository.save(session);
//    }
//
//    private void updateActiveSession(User user, RefreshToken token) {
//        userSessionRepository.findByUserId(user.getId()).ifPresent(session -> {
//            session.setActiveJti(token.getJti());
//            session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            session.setRefreshTokenHash(token.getRefreshHash()); // Add this line
//            userSessionRepository.save(session);
//        });
//    }
//    /* ===========================
//       إنشاء مستخدم جديد (normalized_phone مولَّد DB)
//       =========================== */
//    private User createUserIfNotExists(String phone) {
//        User user = new User();
//        user.setPhoneNumber(phone);
//        user.setCountryCode(phone.substring(0, Math.min(phone.length(), 5)));
//        user.setVerified(true);
//        user.setNormalizedPhone(PhoneUtil.normalizePhone(phone));
//        user.setAccountStatus(AccountStatus.ACTIVE);
//        try {
//            return userRepository.save(user);
//        } catch (DataIntegrityViolationException ex) {
//            // احتمال تسابق: لو انحفظ مستخدم بنفس الهاتف قبلنا مباشرة
//            return userRepository.findByNormalizedPhone(PhoneUtil.normalizePhone(phone))
//                    .orElseThrow(() -> ex);
//        }
//    }
//}
