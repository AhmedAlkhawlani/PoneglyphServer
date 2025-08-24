package com.nova.poneglyph.service.auth;

import com.nova.poneglyph.config.v2.KeyStorageService;
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
import com.nova.poneglyph.service.SessionService;
import com.nova.poneglyph.service.audit.AuditService;
import com.nova.poneglyph.util.EncryptionUtil;
import com.nova.poneglyph.util.JwtUtil;
import com.nova.poneglyph.util.OtpGenerator;
import com.nova.poneglyph.util.PhoneUtil;
import com.nova.poneglyph.util.RateLimiterService;
import io.jsonwebtoken.JwtException;
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
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditService auditService;
    private final SessionService sessionService;

    private final PasswordEncoder passwordEncoder;
    private final EncryptionUtil encryptionUtil;
    private final JwtUtil jwtUtil;
    private final PhoneUtil phoneUtil;
    private final RateLimiterService rateLimiterService;
    private final OtpProcessingService otpProcessingService;
    private final UserDeviceRepository userDeviceRepository;
    private final KeyStorageService keyStorageService;

    @Value("${app.jwt.accessExpirationSeconds:${jwt.access.expiration:1800}}")
    private long accessExpiration;

    @Value("${app.jwt.refreshExpirationSeconds:${jwt.refresh.expiration:1209600}}")
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

        // IMPORTANT: do not log raw OTP in production
        log.info("OTP issued for phone={} (expires in {}m)", maskPhone(normalized), otpExpirationMinutes);

        log.info("OTP  phone={} ", otp);
        // تدقيق
        auditService.logAuthEvent(null, "OTP_REQUEST", "SENT", requestDto.getIp());
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        int len = phone.length();
        String visible = phone.substring(len - 4);
        String maskedPrefix = phone.substring(0, Math.max(0, len - 4)).replaceAll(".", "*");
        return maskedPrefix + visible;
    }

    /* ===========================
       تحقق OTP + إنشاء/جلب المستخدم
       =========================== */
    @Transactional
    public AuthResponseDto verifyOtp(OtpVerifyDto verifyDto) {

        try {
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

            // إدارة الحد الأقصى للجلسات
            List<UserSession> activeSessions = sessionService.findActiveSessionsByUserId(user.getId());
            if (activeSessions.size() >= 3) { // حد أقصى 3 جلسات
                UserSession oldestSession = activeSessions.stream()
                        .min(Comparator.comparing(UserSession::getIssuedAt))
                        .orElse(null);
                if (oldestSession != null) {
                    revokeSession(oldestSession.getId(), user.getId());
                }
            }

            AuthResponseDto response = issueNewTokensTransactional(user, verifyDto.getDeviceId(), verifyDto.getIp());

            auditService.logAuthEvent(user.getId(), "OTP_VERIFY", "SUCCESS", verifyDto.getIp());
            return response;
        } catch (Exception e) {
            log.error("Error in verifyOtp for phone {}: {}", verifyDto.getPhone(), e.getMessage(), e);
            throw new OtpValidationException("Failed to verify OTP " + e.getMessage());
        }

    }

    /* ===========================
       تجديد التوكنات مع التحسينات الأمنية
       =========================== */

    @Transactional
    public AuthResponseDto refreshToken(RefreshRequestDto refreshDto) {
        String raw = refreshDto.getRefreshToken();
        final String jtiStr;

        // 1) محاولة استخراج JTI (قد تقوم JwtUtil بإرجاع null إذا لم تتمكن解析 التوقيع)
        try {
            jtiStr = jwtUtil.extractJti(raw);
        } catch (Exception ex) {
            log.warn("refreshToken: invalid token format: {}", ex.getMessage());
            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_INVALID_FORMAT");
            throw new TokenRefreshException("Invalid refresh token format");
        }

        // 2) تحقق من وجود jtiStr
        if (jtiStr == null || jtiStr.isBlank()) {
            log.warn("refreshToken: jti missing or unreadable");
            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_MISSING_JTI");
            throw new TokenRefreshException("Invalid refresh token format");
        }

        // 3) حاول تحويل jti إلى UUID بأمان
        final UUID jti;
        try {
            jti = UUID.fromString(jtiStr);
        } catch (IllegalArgumentException iae) {
            log.warn("refreshToken: jti not a valid UUID: {}", jtiStr);
            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_INVALID_JTI_FORMAT");
            throw new TokenRefreshException("Invalid refresh token format");
        }

        // 4) Rate limiting بناءً على JTI (آمن لأننا نحصل على jti الآن)
        String rateLimitKey = "refresh_" + jtiStr;
        if (rateLimiterService.isRateLimited(rateLimitKey, 5, Duration.ofMinutes(1))) {
            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "RATE_LIMITED");
            throw new RateLimitExceededException("Too many refresh requests");
        }

        // 5) إيجاد السجل في DB
        RefreshToken dbToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in DB: jti={}", jti);
                    auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_TOKEN_NOT_FOUND");
                    return new TokenRefreshException("Invalid refresh token");
                });

        // 6) الآن التحقق من صحة JWT باستخدام JwtUtil (يشمل المفاتيح المؤرشفة داخلياً)
        boolean jwtValid = false;
        try {
            jwtValid = jwtUtil.validateRefreshToken(raw, dbToken.getUser().getId().toString());
        } catch (JwtException e) {
            log.debug("Primary JWT validation failed: {}", e.getMessage());
            jwtValid = false;
        }

        if (!jwtValid) {
            log.warn("Invalid refresh token JWT after archived-key check: jti={}", jti);
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_INVALID_JWT");
            throw new TokenRefreshException("Invalid refresh token");
        }

        // 7) تحقق من أن التوكن ليس مُلغى أو منتهي على مستوى السجل
        if (dbToken.getRevokedAt() != null) {
            log.warn("Refresh token already revoked: jti={}", jti);
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_ALREADY_REVOKED");
            throw new TokenRefreshException("Refresh token revoked");
        }

        if (dbToken.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            log.warn("Refresh token expired in DB: jti={}", jti);
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_EXPIRED");
            throw new TokenRefreshException("Refresh token expired");
        }

        // 8) مقارنة SHA-256 بالـ hash المخزن
        String candidateHash = encryptionUtil.hash(raw);
        if (!candidateHash.equals(dbToken.getRefreshHash())) {
            log.warn("refreshToken hash mismatch jti={}, user={}", jti, dbToken.getUser().getId());
            revokeAllTokensForUser(dbToken.getUser().getId(), "Token hash mismatch detected");
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REUSE_DETECTED", "All tokens revoked");
            throw new TokenReuseException("Token mismatch / reuse detected");
        }

        // 9) تدوير التوكنات وإرجاع الاستجابة
        AuthResponseDto response = rotateTokens(dbToken, raw);

        // تدقيق النجاح
        auditService.logAuthEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "SUCCESS", dbToken.getIpAddress());
        return response;
    }

    /* ===========================
       تسجيل الخروج
       =========================== */

    @Transactional
    public void logout(String refreshTokenRaw) {
        try {
            String jtiStr = jwtUtil.extractJti(refreshTokenRaw);
            if (jtiStr == null || jtiStr.isBlank()) {
                log.warn("Logout: jti missing or unreadable");
                auditService.logSecurityEvent(null, "LOGOUT", "FAILED_MISSING_JTI");
                throw new TokenRefreshException("Invalid refresh token format");
            }

            UUID jti = UUID.fromString(jtiStr);

            refreshTokenRepository.findByJti(jti).ifPresent(token -> {
                String candidateHash = encryptionUtil.hash(refreshTokenRaw);
                if (candidateHash.equals(token.getRefreshHash())) {
                    // إبطال التوكن
                    token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    refreshTokenRepository.save(token);

                    // استخدام الدالة المحسنة لإلغاء الجلسة والتوكنات
                    sessionService.revokeSessionAndTokensByJti(jti);

                    log.debug("Logout ok: revoked jti={}", jti);
                    auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "SUCCESS");
                } else {
                    log.warn("Logout: token hash mismatch jti={}", jti);
                    auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "FAILED_HASH_MISMATCH");
                }
            });
        } catch (Exception ex) {
            log.error("Logout error: {}", ex.getMessage(), ex);
            auditService.logSecurityEvent(null, "LOGOUT", "FAILED_EXCEPTION: " + ex.getMessage());
            throw new TokenRefreshException("Logout failed");
        }
    }

    /* ===========================
       إلغاء جميع الجلسات
       =========================== */
    @Transactional
    public void revokeAllSessions(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId);
        sessionService.revokeAllSessions(userId);
        auditService.logSecurityEvent(userId, "REVOKE_ALL_SESSIONS", "All sessions and tokens revoked");
    }

    /* ===========================
       الحصول على الجلسات النشطة
       =========================== */
    @Transactional(readOnly = true)
    public List<UserSessionDto> getActiveSessions(UUID userId) {
        return sessionService.getActiveSessions(userId).stream()
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

    /* ===========================
       إلغاء جلسة محددة
       =========================== */
    @Transactional
    public void revokeSession(UUID sessionId, UUID userId) {
        Optional<UserSession> sessionOpt = sessionService.getSessionById(sessionId);

        if (sessionOpt.isEmpty()) {
            return;
        }

        UserSession session = sessionOpt.get();

        if (!session.getUser().getId().equals(userId)) {
            auditService.logSecurityEvent(userId, "REVOKE_SESSION", "FAILED_UNAUTHORIZED");
            throw new AuthorizationException("Cannot revoke another user's session");
        }

        sessionService.revokeSession(sessionId);

        // إلغاء التوكن المرتبط بالجلسة
        refreshTokenRepository.findBySessionId(sessionId).ifPresent(token -> {
            token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
            refreshTokenRepository.save(token);
        });

        auditService.logSecurityEvent(userId, "REVOKE_SESSION", "Session revoked: " + sessionId);
    }

    /* ===========================
       إلغاء جميع التوكنات للمستخدم
       =========================== */
    @Transactional
    public void revokeAllTokensForUser(UUID userId, String reason) {
        log.warn("Revoking all tokens for user {}: {}", userId, reason);
        refreshTokenRepository.revokeAllForUser(userId);
        sessionService.revokeAllSessions(userId);
        auditService.logSecurityEvent(userId, "TOKENS_REVOKED", "All tokens revoked due to: " + reason);
    }

    /* ===========================
       إصدار التوكنات (جلسة واحدة فعّالة)
       =========================== */
    @Transactional
    public AuthResponseDto issueNewTokensTransactional(User user, String deviceId, String ip) {
        try {
            // إنشاء التوكنات
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            // استخراج JTI من التوكن
            String jtiStr = jwtUtil.extractJti(refreshToken);
            UUID jti = UUID.fromString(jtiStr);

            // إلغاء جميع توكنات المستخدم السابقة ما عدا التوكن الحالي
            refreshTokenRepository.revokeAllForUserExcept(user.getId(), jti);

            // حفظ التوكن الجديد
            String refreshHash = encryptionUtil.hash(refreshToken);
            UserDevice userDevice = findOrCreateUserDevice(user, deviceId, ip);
            RefreshToken newToken = saveRefreshToken(user, jti, refreshHash, userDevice, ip);

            // إنشاء أو تحديث الجلسة
            UserSession session = createOrUpdateSession(user, userDevice, newToken, ip);

            // ربط الجلسة مع التوكن وحفظه
            newToken.setSession(session);
            refreshTokenRepository.save(newToken);

            // استخراج تواريخ الانتهاء من التوكنات
            Date accessExp = jwtUtil.extractExpiration(accessToken);
            Date refreshExp = jwtUtil.extractExpiration(refreshToken);

            // إعادة الاستجابة
            return new AuthResponseDto(
                    accessToken,
                    accessExp.getTime(),
                    refreshToken,
                    refreshExp.getTime(),
                    user.getId().toString()
            );

        } catch (JwtException e) {
            log.error("JWT error in issueNewTokensTransactional for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to generate tokens: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid JTI format in issueNewTokensTransactional for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Invalid token format", e);
        }
    }

    private UserDevice findOrCreateUserDevice(User user, String deviceId, String ip) {
        return userDeviceRepository.findByUserAndDeviceId(user, deviceId)
                .orElseGet(() -> userDeviceRepository.save(
                        UserDevice.builder()
                                .user(user)
                                .deviceId(deviceId)
                                .ipAddress(ip)
                                .lastLogin(OffsetDateTime.now(ZoneOffset.UTC))
                                .active(true)
                                .build()
                ));
    }

    private UserSession createOrUpdateSession(User user, UserDevice device, RefreshToken token, String ip) {
        Optional<UserSession> existingSession = sessionService.findByUserAndDevice(user, device);

        // التحقق من عدد الجلسات النشطة قبل الإنشاء
//        long activeSessions = sessionService.countActiveSessionsByUserId(user.getId());
//        if (activeSessions >= 3 && existingSession.isEmpty()) {
//            throw new TooManySessionsException("Maximum active sessions reached");
//        }

        UserSession session;
        if (existingSession.isPresent()) {
            session = existingSession.get();
            session.setActiveJti(token.getJti());
            session.setIpAddress(ip);
            session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
            session.setExpiresAt(token.getExpiresAt());
            session.setRefreshTokenHash(token.getRefreshHash());
            session.setActive(true);
        } else {
            session = UserSession.builder()
                    .user(user)
                    .device(device)
                    .activeJti(token.getJti())
                    .ipAddress(ip)
                    .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .lastUsedAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .expiresAt(token.getExpiresAt())
                    .refreshTokenHash(token.getRefreshHash())
                    .active(true)
                    .build();
        }

        return sessionService.saveSession(session);
    }

    @Transactional
    public void revokeAllActiveSessionsAndTokens(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId);
        sessionService.revokeAllActiveSessionsAndTokens(userId);
    }

    /* ===========================
       تدوير التوكنات (لعملية التجديد)
       =========================== */
    private AuthResponseDto rotateTokens(RefreshToken oldToken, String rawOldRefresh) {
        String newAccessToken = jwtUtil.generateAccessToken(oldToken.getUser());
        String newRefreshToken = jwtUtil.generateRefreshToken(oldToken.getUser());

        String newJtiStr = jwtUtil.extractJti(newRefreshToken);
        UUID newJti = UUID.fromString(newJtiStr);

        refreshTokenRepository.revokeAllForUserExcept(oldToken.getUser().getId(), newJti);

        String newRefreshHash = encryptionUtil.hash(newRefreshToken);
        UserDevice device = oldToken.getDevice();

        RefreshToken newToken = saveRefreshToken(oldToken.getUser(), newJti, newRefreshHash, device, oldToken.getIpAddress());

        // تحديث الجلسة النشطة
        updateActiveSession(oldToken.getUser(), newToken);

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
                .device(device)
                .ipAddress(ip)
                .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(refreshExpiration))
                .build();
        return refreshTokenRepository.save(token);
    }

    private void updateActiveSession(User user, RefreshToken token) {
        Optional<UserSession> sessionOpt = sessionService.findLatestActiveSessionByUserId(user.getId());

        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setActiveJti(token.getJti());
            session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
            session.setRefreshTokenHash(token.getRefreshHash());
            session.setExpiresAt(token.getExpiresAt());
            if (token.getDevice() != null) {
                session.setDevice(token.getDevice());
            }
            sessionService.saveSession(session);

            // ربط التوكن بالجلسة
            token.setSession(session);
            refreshTokenRepository.save(token);
        } else {
            // لا تغيير — إذا أردت إنشاء جلسة جديدة هنا فبإمكانك تفعيل الكود التالي
            // UserSession newSession = createNewSession(user, token);
            // sessionService.saveSession(newSession);
            // token.setSession(newSession);
            // refreshTokenRepository.save(token);
        }
    }

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
            return userRepository.findByNormalizedPhone(PhoneUtil.normalizePhone(phone))
                    .orElseThrow(() -> ex);
        }
    }

    // دالة مساعدة لتوليد JTI جديدة
    public String newJti() {
        return UUID.randomUUID().toString();
    }
}
