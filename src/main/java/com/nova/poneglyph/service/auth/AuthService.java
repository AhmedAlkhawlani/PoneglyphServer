//package com.nova.poneglyph.service.auth;
//
//import com.nova.poneglyph.config.v2.KeyStorageService;
//import com.nova.poneglyph.domain.auth.OtpCode;
//import com.nova.poneglyph.domain.auth.RefreshToken;
//import com.nova.poneglyph.domain.enums.AccountStatus;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.domain.user.UserDevice;
//import com.nova.poneglyph.domain.user.UserProfile;
//import com.nova.poneglyph.domain.user.UserSession;
//import com.nova.poneglyph.dto.authDto.AuthResponseDto;
//import com.nova.poneglyph.dto.authDto.OtpRequestDto;
//import com.nova.poneglyph.dto.authDto.OtpVerifyDto;
//import com.nova.poneglyph.dto.authDto.RefreshRequestDto;
//import com.nova.poneglyph.dto.userDto.UserSessionDto;
//import com.nova.poneglyph.exception.*;
//import com.nova.poneglyph.repository.*;
//import com.nova.poneglyph.service.SessionService;
//import com.nova.poneglyph.service.audit.AuditService;
//import com.nova.poneglyph.util.EncryptionUtil;
//import com.nova.poneglyph.util.JwtUtil;
//import com.nova.poneglyph.util.OtpGenerator;
//import com.nova.poneglyph.util.PhoneUtil;
//import com.nova.poneglyph.util.RateLimiterService;
//import io.jsonwebtoken.JwtException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.security.MessageDigest;
//import java.time.Duration;
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.*;
//
//import static com.nova.poneglyph.util.PhoneUtil.MIN_PHONE_LENGTH;
//
//@Log4j2
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final UserRepository userRepository;
//    private final OtpCodeRepository otpCodeRepository;
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final AuditService auditService;
//    private final SessionService sessionService;
//
//    private final PasswordEncoder passwordEncoder;
//    private final EncryptionUtil encryptionUtil;
//    private final JwtUtil jwtUtil;
//    private final RateLimiterService rateLimiterService;
//    private final OtpProcessingService otpProcessingService;
//    private final UserDeviceRepository userDeviceRepository;
//
//    private final UserProfileRepository userProfileRepository;
//
//    @Value("${app.jwt.accessExpirationSeconds:${jwt.access.expiration:1800}}")
//    private long accessExpiration;
//
//    @Value("${app.jwt.refreshExpirationSeconds:${jwt.refresh.expiration:1209600}}")
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
//        otpCodeRepository.save(otpCode);
//
//        // IMPORTANT: do not log raw OTP in production
//        log.info("OTP issued for phone={} (expires in {}m)", maskPhone(normalized), otpExpirationMinutes);
//
//        log.info("OTP  phone={} ", otp);
//        log.debug("OTP (masked) for phone {}: {}", maskPhone(normalized), "***");
//
//        // تدقيق
//        auditService.logAuthEvent(null, "OTP_REQUEST", "SENT", requestDto.getIp());
//    }
//
//    private String maskPhone(String phone) {
//        if (phone == null || phone.length() < 4) return "***";
//        int len = phone.length();
//        String visible = phone.substring(len - 4);
//        String maskedPrefix = phone.substring(0, Math.max(0, len - 4)).replaceAll(".", "*");
//        return maskedPrefix + visible;
//    }
//
//    /* ===========================
//       تحقق OTP + إنشاء/جلب المستخدم
//       =========================== */
//
//
//    @Transactional
//    public AuthResponseDto verifyOtp(OtpVerifyDto verifyDto) {
//        try {
//            // التحقق من صحة رقم الهاتف
//            String normalized = validateAndNormalizePhone(verifyDto.getPhone());
//
//            // التحقق من OTP
//            validateOtp(normalized, verifyDto.getCode(), verifyDto.getIp());
//
//            // الحصول على المستخدم أو إنشائه
//            User user = getUserOrCreate(verifyDto.getPhone(), normalized);
//
//            // تحديث حالة التحقق إذا لزم الأمر
//            updateUserVerificationStatus(user);
//
//            // إنشاء/تحديث جهاز المستخدم
//            UserDevice userDevice = createOrUpdateUserDevice(user, verifyDto);
//
//            // إدارة الجلسات النشطة
//            manageActiveSessions(user);
//
//            // إصدار التوكنات الجديدة
//            AuthResponseDto response = issueNewTokensTransactional(user, verifyDto.getDeviceId(), verifyDto.getIp());
//
//            // تسجيل نجاح العملية
//            auditService.logAuthEvent(user.getId(), "OTP_VERIFY", "SUCCESS", verifyDto.getIp());
//
//            return response;
//        } catch (Exception e) {
//            log.error("Error in verifyOtp for phone {}: {}", verifyDto.getPhone(), e.getMessage(), e);
//            throw new OtpValidationException("Failed to verify OTP " + e.getMessage());
//        }
//    }
//
//    // دوال مساعدة
//    private String validateAndNormalizePhone(String phone) {
//        String normalized = PhoneUtil.normalizePhone(phone);
//        if (normalized.length() < MIN_PHONE_LENGTH) {
//            throw new InvalidPhoneNumberException("Phone number is too short");
//        }
//        return normalized;
//    }
//
//    private void validateOtp(String normalizedPhone, String code, String ip) {
//        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
//        Optional<OtpCode> otpOptional = otpCodeRepository.findLatestValidOtp(normalizedPhone, now);
//
//        if (otpOptional.isEmpty()) {
//            auditService.logAuthEvent(null, "OTP_VERIFY", "FAILED_NO_OTP", ip);
//            throw new OtpValidationException("Invalid or expired OTP");
//        }
//
//        OtpProcessingService.OtpCheckResult checkResult =
//                otpProcessingService.processOtpAttemptAndMaybeConsume(otpOptional.get().getId(), code);
//
//        switch (checkResult) {
//            case INVALID -> {
//                auditService.logAuthEvent(null, "OTP_VERIFY", "FAILED_INVALID", ip);
//                throw new OtpValidationException("Invalid OTP code");
//            }
//            case TOO_MANY_ATTEMPTS, EXPIRED, NOT_FOUND -> {
//                auditService.logAuthEvent(null, "OTP_VERIFY", "FAILED_" + checkResult.name(), ip);
//                throw new OtpValidationException("Invalid or expired OTP");
//            }
//            case SUCCESS -> { /* proceed */ }
//        }
//    }
//
//    private User getUserOrCreate(String phone, String normalizedPhone) {
//        return userRepository.findByNormalizedPhone(normalizedPhone)
//                .orElseGet(() -> createUserIfNotExists(phone));
//    }
//
//    private void updateUserVerificationStatus(User user) {
//        if (!user.isVerified()) {
//            user.setVerified(true);
//            userRepository.save(user);
//        }
//    }
//
//    private UserDevice createOrUpdateUserDevice(User user, OtpVerifyDto verifyDto) {
//        return userDeviceRepository.findByUserAndDeviceId(user, verifyDto.getDeviceId())
//                .map(existingDevice -> updateExistingDevice(existingDevice, verifyDto))
//                .orElseGet(() -> createNewDevice(user, verifyDto));
//    }
//
//    private UserDevice updateExistingDevice(UserDevice device, OtpVerifyDto verifyDto) {
//        device.setDeviceKey(verifyDto.getDeviceKey());
//        device.setDeviceFingerprint(verifyDto.getDeviceFingerprint());
//        device.setIpAddress(verifyDto.getIp());
//        device.setOsVersion(verifyDto.getOsVersion());
//        device.setAppVersion(verifyDto.getAppVersion());
//        device.setLastLogin(OffsetDateTime.now(ZoneOffset.UTC));
//        return userDeviceRepository.save(device);
//    }
//
//    private UserDevice createNewDevice(User user, OtpVerifyDto verifyDto) {
//        return userDeviceRepository.save(
//                UserDevice.builder()
//                        .user(user)
//                        .deviceId(verifyDto.getDeviceId())
//                        .deviceKey(verifyDto.getDeviceKey())
//                        .deviceFingerprint(verifyDto.getDeviceFingerprint())
//                        .ipAddress(verifyDto.getIp())
//                        .osVersion(verifyDto.getOsVersion())
//                        .appVersion(verifyDto.getAppVersion())
//                        .lastLogin(OffsetDateTime.now(ZoneOffset.UTC))
//                        .active(true)
//                        .build()
//        );
//    }
//
//    private void manageActiveSessions(User user) {
//        List<UserSession> activeSessions = sessionService.findActiveSessionsByUserId(user.getId());
//        if (activeSessions.size() == 1) {
////        if (activeSessions.size() >= 3) {
//            UserSession oldestSession = activeSessions.stream()
//                    .min(Comparator.comparing(UserSession::getIssuedAt))
//                    .orElse(null);
//            if (oldestSession != null) {
//                revokeSession(oldestSession.getId(), user.getId());
//            }
//        }
//    }
//
//
//    /* ===========================
//       تجديد التوكنات مع التحسينات الأمنية
//       =========================== */
//
//
//@Transactional
//public AuthResponseDto refreshToken(RefreshRequestDto refreshDto) {
//    String rawToken = refreshDto.getRefreshToken();
//
//    try {
//        // استخراج وتحقق JTI
//        String jtiStr = extractAndValidateJti(rawToken);
//        UUID jti = parseJti(jtiStr);
//
//        // التحقق من معدل الطلبات
//        checkRateLimit(jtiStr);
//
//        // البحث عن التوكن في قاعدة البيانات
//        RefreshToken dbToken = findRefreshToken(jti);
//
//        // التحقق من صحة التوكن
//        validateRefreshToken(rawToken, dbToken);
//
//        // تدوير التوكنات
//        AuthResponseDto response = rotateTokens(dbToken, rawToken);
//
//        // تسجيل النجاح
//        auditService.logAuthEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "SUCCESS", dbToken.getIpAddress());
//
//        return response;
//    } catch (TokenRefreshException e) {
//        throw e;
//    } catch (Exception e) {
//        log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
//        throw new TokenRefreshException("Token refresh failed due to an unexpected error");
//    }
//}
//
//    // دوال مساعدة لـ refreshToken
//    private String extractAndValidateJti(String token) {
//        try {
//            String jtiStr = jwtUtil.extractJti(token);
//            if (jtiStr == null || jtiStr.isBlank()) {
//                log.warn("Refresh token: jti missing or unreadable");
//                auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_MISSING_JTI");
//                throw new TokenRefreshException("Invalid refresh token format");
//            }
//            return jtiStr;
//        } catch (Exception ex) {
//            log.warn("Refresh token: invalid token format: {}", ex.getMessage());
//            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_INVALID_FORMAT");
//            throw new TokenRefreshException("Invalid refresh token format");
//        }
//    }
//
//    private UUID parseJti(String jtiStr) {
//        try {
//            return UUID.fromString(jtiStr);
//        } catch (IllegalArgumentException iae) {
//            log.warn("Refresh token: jti not a valid UUID: {}", jtiStr);
//            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_INVALID_JTI_FORMAT");
//            throw new TokenRefreshException("Invalid refresh token format");
//        }
//    }
//
//    private void checkRateLimit(String jtiStr) {
//        String rateLimitKey = "refresh_" + jtiStr;
//        if (rateLimiterService.isRateLimited(rateLimitKey, 5, Duration.ofMinutes(1))) {
//            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "RATE_LIMITED");
//            throw new RateLimitExceededException("Too many refresh requests");
//        }
//    }
//
//    private RefreshToken findRefreshToken(UUID jti) {
//        return refreshTokenRepository.findByJti(jti)
//                .orElseThrow(() -> {
//                    log.warn("Refresh token not found in DB: jti={}", jti);
//                    auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_TOKEN_NOT_FOUND");
//                    return new TokenRefreshException("Invalid refresh token");
//                });
//    }
//
//    private void validateRefreshToken(String rawToken, RefreshToken dbToken) {
//        // التحقق من صحة JWT
//        boolean jwtValid = jwtUtil.validateRefreshToken(rawToken, dbToken.getUser().getId().toString());
//        if (!jwtValid) {
//            log.warn("Invalid refresh token JWT: jti={}", dbToken.getJti());
//            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_INVALID_JWT");
//            throw new TokenRefreshException("Invalid refresh token");
//        }
//
//        // التحقق من حالة التوكن
//        if (dbToken.getRevokedAt() != null) {
//            log.warn("Refresh token already revoked: jti={}", dbToken.getJti());
//            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_ALREADY_REVOKED");
//            throw new TokenRefreshException("Refresh token revoked");
//        }
//
//        if (dbToken.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
//            log.warn("Refresh token expired in DB: jti={}", dbToken.getJti());
//            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_EXPIRED");
//            throw new TokenRefreshException("Refresh token expired");
//        }
//
//
//
//        // التحقق من hash التوكن
//        String candidateHash = encryptionUtil.hash(rawToken);
//        if (!secureEqualsBase64(candidateHash, dbToken.getRefreshHash())) {
//            log.warn("Refresh token hash mismatch jti={}, user={}", dbToken.getJti(), dbToken.getUser().getId());
//            revokeAllTokensForUser(dbToken.getUser().getId(), "Token hash mismatch detected");
//            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REUSE_DETECTED", "All tokens revoked");
//            throw new TokenReuseException("Token mismatch / reuse detected");
//        }
//    }
//    // دالة مساعدة ثابتة الزمن للمقارنة (Base64-encoded digests)
//    private static boolean secureEqualsBase64(String aBase64, String bBase64) {
//        if (aBase64 == null || bBase64 == null) return false;
//        try {
//            byte[] a = Base64.getDecoder().decode(aBase64);
//            byte[] b = Base64.getDecoder().decode(bBase64);
//            return MessageDigest.isEqual(a, b);
//        } catch (IllegalArgumentException ex) {
//            // فشل في فك Base64 => لا يطابق
//            return false;
//        }
//    }
//    /* ===========================
//       تسجيل الخروج
//       =========================== */
//
//    @Transactional
//    public void logout(String refreshTokenRaw) {
//        try {
//            String jtiStr = jwtUtil.extractJti(refreshTokenRaw);
//            if (jtiStr == null || jtiStr.isBlank()) {
//                log.warn("Logout: jti missing or unreadable");
//                auditService.logSecurityEvent(null, "LOGOUT", "FAILED_MISSING_JTI");
//                throw new TokenRefreshException("Invalid refresh token format");
//            }
//
//            UUID jti = UUID.fromString(jtiStr);
//
//            refreshTokenRepository.findByJti(jti).ifPresent(token -> {
//                String candidateHash = encryptionUtil.hash(refreshTokenRaw);
//
//                byte[] candidate = Base64.getDecoder().decode(candidateHash);
//                byte[] stored = Base64.getDecoder().decode(token.getRefreshHash());
//
//                if (!MessageDigest.isEqual(candidate, stored)) {
//                    log.warn("Refresh token hash mismatch jti={}, user={}", token.getJti(), token.getUser().getId());
//                    // إبطال التوكن
//                    token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//                    refreshTokenRepository.save(token);
//
//                    // استخدام الدالة المحسنة لإلغاء الجلسة والتوكنات
//                    sessionService.revokeSessionAndTokensByJti(jti);
//
//                    log.debug("Logout ok: revoked jti={}", jti);
//                    auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "SUCCESS");
////                    throw new TokenReuseException("Token mismatch / reuse detected");
//                }else {
//                    log.warn("Logout: token hash mismatch jti={}", jti);
//                    auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "FAILED_HASH_MISMATCH");
//
//                }
////                if (candidateHash.equals(token.getRefreshHash())) {
////                    // إبطال التوكن
////                    token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
////                    refreshTokenRepository.save(token);
////
////                    // استخدام الدالة المحسنة لإلغاء الجلسة والتوكنات
////                    sessionService.revokeSessionAndTokensByJti(jti);
////
////                    log.debug("Logout ok: revoked jti={}", jti);
////                    auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "SUCCESS");
////                } else {
////                    log.warn("Logout: token hash mismatch jti={}", jti);
////                    auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "FAILED_HASH_MISMATCH");
////                }
//            });
//        } catch (Exception ex) {
//            log.error("Logout error: {}", ex.getMessage(), ex);
//            auditService.logSecurityEvent(null, "LOGOUT", "FAILED_EXCEPTION: " + ex.getMessage());
//            throw new TokenRefreshException("Logout failed");
//        }
//    }
//
//    /* ===========================
//       إلغاء جميع الجلسات
//       =========================== */
//    @Transactional
//    public void revokeAllSessions(UUID userId) {
//        refreshTokenRepository.revokeAllForUser(userId);
//        sessionService.revokeAllSessions(userId);
//        auditService.logSecurityEvent(userId, "REVOKE_ALL_SESSIONS", "All sessions and tokens revoked");
//    }
//
//    /* ===========================
//       الحصول على الجلسات النشطة
//       =========================== */
//    @Transactional(readOnly = true)
//    public List<UserSessionDto> getActiveSessions(UUID userId) {
//        return sessionService.getActiveSessions(userId).stream()
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
//    /* ===========================
//       إلغاء جلسة محددة
//       =========================== */
//    @Transactional
//    public void revokeSession(UUID sessionId, UUID userId) {
//        Optional<UserSession> sessionOpt = sessionService.getSessionById(sessionId);
//
//        if (sessionOpt.isEmpty()) {
//            return;
//        }
//
//        UserSession session = sessionOpt.get();
//
//        if (!session.getUser().getId().equals(userId)) {
//            auditService.logSecurityEvent(userId, "REVOKE_SESSION", "FAILED_UNAUTHORIZED");
//            throw new AuthorizationException("Cannot revoke another user's session");
//        }
//
//        sessionService.revokeSession(sessionId);
//
//        // إلغاء التوكن المرتبط بالجلسة
//        refreshTokenRepository.findBySessionId(sessionId).ifPresent(token -> {
//            token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            refreshTokenRepository.save(token);
//        });
//
//        auditService.logSecurityEvent(userId, "REVOKE_SESSION", "Session revoked: " + sessionId);
//    }
//
//    /* ===========================
//       إلغاء جميع التوكنات للمستخدم
//       =========================== */
//    @Transactional
//    public void revokeAllTokensForUser(UUID userId, String reason) {
//        log.warn("Revoking all tokens for user {}: {}", userId, reason);
//        refreshTokenRepository.revokeAllForUser(userId);
//        sessionService.revokeAllSessions(userId);
//        auditService.logSecurityEvent(userId, "TOKENS_REVOKED", "All tokens revoked due to: " + reason);
//    }
//
//    /* ===========================
//       إصدار التوكنات (جلسة واحدة فعّالة)
//       =========================== */
//    @Transactional
//    public AuthResponseDto issueNewTokensTransactional(User user, String deviceId, String ip) {
//        try {
//            // إنشاء التوكنات
//            String accessToken = jwtUtil.generateAccessToken(user);
//            String refreshToken = jwtUtil.generateRefreshToken(user);
//
//            // استخراج JTI من التوكن
//            String jtiStr = jwtUtil.extractJti(refreshToken);
//            UUID jti = UUID.fromString(jtiStr);
//
//            // إلغاء جميع توكنات المستخدم السابقة ما عدا التوكن الحالي
//            refreshTokenRepository.revokeAllForUserExcept(user.getId(), jti);
//
//            // حفظ التوكن الجديد
//            String refreshHash = encryptionUtil.hash(refreshToken);
//            UserDevice userDevice = findOrCreateUserDevice(user, deviceId, ip);
//            RefreshToken newToken = saveRefreshToken(user, jti, refreshHash, userDevice, ip);
//
//            // إنشاء أو تحديث الجلسة
//            UserSession session = createOrUpdateSession(user, userDevice, newToken, ip);
//
//            // ربط الجلسة مع التوكن وحفظه
//            newToken.setSession(session);
//            refreshTokenRepository.save(newToken);
//
//            // استخراج تواريخ الانتهاء من التوكنات
//            Date accessExp = jwtUtil.extractExpiration(accessToken);
//            Date refreshExp = jwtUtil.extractExpiration(refreshToken);
//
//            // إعادة الاستجابة
//            return new AuthResponseDto(
//                    accessToken,
//                    accessExp.getTime(),
//                    refreshToken,
//                    refreshExp.getTime(),
//                    user.getId().toString()
//            );
//
//        } catch (JwtException e) {
//            log.error("JWT error in issueNewTokensTransactional for user {}: {}", user.getId(), e.getMessage());
//            throw new RuntimeException("Failed to generate tokens: " + e.getMessage(), e);
//        } catch (IllegalArgumentException e) {
//            log.error("Invalid JTI format in issueNewTokensTransactional for user {}: {}", user.getId(), e.getMessage());
//            throw new RuntimeException("Invalid token format", e);
//        }
//    }
//
//    private UserDevice findOrCreateUserDevice(User user, String deviceId, String ip) {
//        return userDeviceRepository.findByUserAndDeviceId(user, deviceId)
//                .orElseGet(() -> userDeviceRepository.save(
//                        UserDevice.builder()
//                                .user(user)
//                                .deviceId(deviceId)
//                                .ipAddress(ip)
//                                .lastLogin(OffsetDateTime.now(ZoneOffset.UTC))
//                                .active(true)
//                                .build()
//                ));
//    }
//
//    private UserSession createOrUpdateSession(User user, UserDevice device, RefreshToken token, String ip) {
//        Optional<UserSession> existingSession = sessionService.findByUserAndDevice(user, device);
//
//        // التحقق من عدد الجلسات النشطة قبل الإنشاء
////        long activeSessions = sessionService.countActiveSessionsByUserId(user.getId());
////        if (activeSessions >= 3 && existingSession.isEmpty()) {
////            throw new TooManySessionsException("Maximum active sessions reached");
////        }
//
//        UserSession session;
//        if (existingSession.isPresent()) {
//            session = existingSession.get();
//            session.setActiveJti(token.getJti());
//            session.setIpAddress(ip);
//            session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            session.setExpiresAt(token.getExpiresAt());
//            session.setRefreshTokenHash(token.getRefreshHash());
//            session.setActive(true);
//        } else {
//            session = UserSession.builder()
//                    .user(user)
//                    .device(device)
//                    .activeJti(token.getJti())
//                    .ipAddress(ip)
//                    .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
//                    .lastUsedAt(OffsetDateTime.now(ZoneOffset.UTC))
//                    .expiresAt(token.getExpiresAt())
//                    .refreshTokenHash(token.getRefreshHash())
//                    .active(true)
//                    .build();
//        }
//
//        return sessionService.saveSession(session);
//    }
//
//    @Transactional
//    public void revokeAllActiveSessionsAndTokens(UUID userId) {
//        refreshTokenRepository.revokeAllForUser(userId);
//        sessionService.revokeAllActiveSessionsAndTokens(userId);
//    }
//
//    /* ===========================
//       تدوير التوكنات (لعملية التجديد)
//       =========================== */
//    private AuthResponseDto rotateTokens(RefreshToken oldToken, String rawOldRefresh) {
//        String newAccessToken = jwtUtil.generateAccessToken(oldToken.getUser());
//        String newRefreshToken = jwtUtil.generateRefreshToken(oldToken.getUser());
//
//        String newJtiStr = jwtUtil.extractJti(newRefreshToken);
//        UUID newJti = UUID.fromString(newJtiStr);
//
//        refreshTokenRepository.revokeAllForUserExcept(oldToken.getUser().getId(), newJti);
//
//        String newRefreshHash = encryptionUtil.hash(newRefreshToken);
//        UserDevice device = oldToken.getDevice();
//
//        RefreshToken newToken = saveRefreshToken(oldToken.getUser(), newJti, newRefreshHash, device, oldToken.getIpAddress());
//
//        // تحديث الجلسة النشطة
//        updateActiveSession(oldToken.getUser(), newToken);
//
//        oldToken.setReplacedBy(newToken.getJti());
//        oldToken.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
//        refreshTokenRepository.save(oldToken);
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
//    private RefreshToken saveRefreshToken(User user, UUID jti, String hash, UserDevice device, String ip) {
//        RefreshToken token = RefreshToken.builder()
//                .jti(jti)
//                .user(user)
//                .refreshHash(hash)
//                .device(device)
//                .ipAddress(ip)
//                .issuedAt(OffsetDateTime.now(ZoneOffset.UTC))
//                .expiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(refreshExpiration))
//                .build();
//        return refreshTokenRepository.save(token);
//    }
//
//    private void updateActiveSession(User user, RefreshToken token) {
//        Optional<UserSession> sessionOpt = sessionService.findLatestActiveSessionByUserId(user.getId());
//
//        if (sessionOpt.isPresent()) {
//            UserSession session = sessionOpt.get();
//            session.setActiveJti(token.getJti());
//            session.setLastUsedAt(OffsetDateTime.now(ZoneOffset.UTC));
//            session.setRefreshTokenHash(token.getRefreshHash());
//            session.setExpiresAt(token.getExpiresAt());
//            if (token.getDevice() != null) {
//                session.setDevice(token.getDevice());
//            }
//            sessionService.saveSession(session);
//
//            // ربط التوكن بالجلسة
//            token.setSession(session);
//            refreshTokenRepository.save(token);
//        } else {
//            // لا تغيير — إذا أردت إنشاء جلسة جديدة هنا فبإمكانك تفعيل الكود التالي
//            // UserSession newSession = createNewSession(user, token);
//            // sessionService.saveSession(newSession);
//            // token.setSession(newSession);
//            // refreshTokenRepository.save(token);
//        }
//    }
//
//    private User createUserIfNotExists(String phone) {
//        User user = new User();
//        user.setPhoneNumber(phone);
//
//        // استخراج رمز الدولة من رقم الهاتف
//        String countryCode = PhoneUtil.extractCountryCode(phone);
//        if (countryCode == null) {
//            // إذا لم يتمكن من استخراج رمز الدولة، نستخدم رمز افتراضي (مثل 966 للمملكة العربية السعودية)
//            countryCode = "966";
//        }
//        user.setCountryCode(countryCode);
//
//        user.setVerified(true);
//
//        // توحيد تنسيق رقم الهاتف
//        String normalizedPhone = PhoneUtil.normalizePhone(phone);
//        user.setNormalizedPhone(normalizedPhone);
//
//        user.setAccountStatus(AccountStatus.ACTIVE);
//        try {
//
//            User savedUser=userRepository.save(user);
//            // إنشاء ملف تعريف افتراضي للمستخدم الجديد
//            UserProfile defaultProfile = new UserProfile();
//            defaultProfile.setUser(savedUser);
//            defaultProfile.setDisplayName(savedUser.getDisplayName());
//            defaultProfile.setAboutText("");
//            defaultProfile.setStatusEmoji("");
//            defaultProfile.setLastProfileUpdate(OffsetDateTime.now());
//
//            userProfileRepository.save(defaultProfile);
//            return savedUser;
//        } catch (DataIntegrityViolationException ex) {
//            return userRepository.findByNormalizedPhone(normalizedPhone)
//                    .orElseThrow(() -> ex);
//        }
//    }
//
//    // دالة مساعدة لتوليد JTI جديدة
//    public String newJti() {
//        return UUID.randomUUID().toString();
//    }
//}
package com.nova.poneglyph.service.auth;

import com.nova.poneglyph.domain.auth.OtpCode;
import com.nova.poneglyph.domain.auth.RefreshToken;
import com.nova.poneglyph.domain.enums.AccountStatus;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;
import com.nova.poneglyph.domain.user.UserProfile;
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

import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.nova.poneglyph.util.PhoneUtil.MIN_PHONE_LENGTH;

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
    private final RateLimiterService rateLimiterService;
    private final OtpProcessingService otpProcessingService;
    private final UserDeviceRepository userDeviceRepository;
    private final UserProfileRepository userProfileRepository;

    // Constants / defaults
    private static final String APP_DEFAULT_REGION = "YE"; // ISO region fallback (مثلاً "YE" لليمن)
    private static final String APP_DEFAULT_COUNTRY_NUMERIC = "967"; // numeric country code fallback

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
//        String e164 = PhoneUtil.normalizeToE164(requestDto.getPhone(), APP_DEFAULT_REGION);
//        String normalized = PhoneUtil.normalizeForStorage(requestDto.getPhone(), APP_DEFAULT_REGION);

        // استخرج المنطقة مباشرة من الرقم المرسل
        String defaultRegion = PhoneUtil.extractRegionFromE164(requestDto.getPhone());

        String normalized = PhoneUtil.normalizeForStorage(requestDto.getPhone(), defaultRegion);
        String e164 = PhoneUtil.normalizeToE164(requestDto.getPhone(), APP_DEFAULT_REGION);

        if (normalized == null) {
            throw new InvalidPhoneNumberException("Phone number is invalid");
        }

        if (rateLimiterService.isRateLimited("otp_request:" + normalized, 8, Duration.ofMinutes(15))) {
            throw new RateLimitExceededException("Too many OTP requests");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = now.plusMinutes(otpExpirationMinutes);

        String otp = OtpGenerator.generate(6);
        String otpHash = passwordEncoder.encode(otp);

        OtpCode otpCode = OtpCode.builder()
                .phoneNumber(e164 != null ? e164 : requestDto.getPhone())
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

        // IMPORTANT: do not log raw OTP in production - here only for debug
        log.info("OTP issued for phone={} (expires in {}m)", maskPhone(normalized), otpExpirationMinutes);
        log.debug("OTP (masked) for phone {}: {}", maskPhone(normalized), "***");

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
            // التحقق من صحة رقم الهاتف (normalizeForStorage)
            String normalized = validateAndNormalizePhone(verifyDto.getPhone());

            // التحقق من OTP
            validateOtp(normalized, verifyDto.getCode(), verifyDto.getIp());

            // الحصول على المستخدم أو إنشائه
            User user = getUserOrCreate(verifyDto.getPhone(), normalized);

            // تحديث حالة التحقق إذا لزم الأمر
            updateUserVerificationStatus(user);

            // إنشاء/تحديث جهاز المستخدم
            UserDevice userDevice = createOrUpdateUserDevice(user, verifyDto);

            // إدارة الجلسات النشطة
            manageActiveSessions(user);

            // إصدار التوكنات الجديدة
            AuthResponseDto response = issueNewTokensTransactional(user, verifyDto.getDeviceId(), verifyDto.getIp());

            // تسجيل نجاح العملية
            auditService.logAuthEvent(user.getId(), "OTP_VERIFY", "SUCCESS", verifyDto.getIp());

            return response;
        }catch (OtpValidationException validationException) {
            throw validationException;
        }
        catch (Exception e) {
            log.error("Error in verifyOtp for phone {}: {}", verifyDto.getPhone(), e.getMessage(), e);
            throw new OtpValidationException("Failed to verify OTP " + e.getMessage());
        }
    }

    // دوال مساعدة
    private String validateAndNormalizePhone(String phone) {
//        String normalized = PhoneUtil.normalizeForStorage(phone, APP_DEFAULT_REGION);
        // استخرج المنطقة مباشرة من الرقم المرسل
        String defaultRegion = PhoneUtil.extractRegionFromE164(phone);

        String normalized = PhoneUtil.normalizeForStorage(phone, defaultRegion);

        if (normalized == null || normalized.length() < MIN_PHONE_LENGTH) {
            throw new InvalidPhoneNumberException("Phone number is invalid or too short");
        }
        return normalized;
    }

    private void validateOtp(String normalizedPhone, String code, String ip) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Optional<OtpCode> otpOptional = otpCodeRepository.findLatestValidOtp(normalizedPhone, now);

        if (otpOptional.isEmpty()) {
            auditService.logAuthEvent(null, "OTP_VERIFY", "FAILED_NO_OTP", ip);
            throw new OtpValidationException("Invalid or expired OTP");
        }

        OtpProcessingService.OtpCheckResult checkResult =
                otpProcessingService.processOtpAttemptAndMaybeConsume(otpOptional.get().getId(), code);

        switch (checkResult) {
            case INVALID -> {
                auditService.logAuthEvent(null, "OTP_VERIFY", "FAILED_INVALID", ip);
                throw new OtpValidationException("Invalid OTP code");
            }
            case TOO_MANY_ATTEMPTS, EXPIRED, NOT_FOUND -> {
                auditService.logAuthEvent(null, "OTP_VERIFY", "FAILED_" + checkResult.name(), ip);
                throw new OtpValidationException("Invalid or expired OTP");
            }
            case SUCCESS -> { /* proceed */ }
        }
    }

    private User getUserOrCreate(String phone, String normalizedPhone) {
        return userRepository.findByNormalizedPhone(normalizedPhone)
                .orElseGet(() -> createUserIfNotExists(phone));
    }

    private void updateUserVerificationStatus(User user) {
        if (!user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);
        }
    }

    private UserDevice createOrUpdateUserDevice(User user, OtpVerifyDto verifyDto) {
        return userDeviceRepository.findByUserAndDeviceId(user, verifyDto.getDeviceId())
                .map(existingDevice -> updateExistingDevice(existingDevice, verifyDto))
                .orElseGet(() -> createNewDevice(user, verifyDto));
    }

    private UserDevice updateExistingDevice(UserDevice device, OtpVerifyDto verifyDto) {
        device.setDeviceKey(verifyDto.getDeviceKey());
        device.setDeviceFingerprint(verifyDto.getDeviceFingerprint());
        device.setIpAddress(verifyDto.getIp());
        device.setOsVersion(verifyDto.getOsVersion());
        device.setAppVersion(verifyDto.getAppVersion());
        device.setLastLogin(OffsetDateTime.now(ZoneOffset.UTC));
        return userDeviceRepository.save(device);
    }

    private UserDevice createNewDevice(User user, OtpVerifyDto verifyDto) {
        return userDeviceRepository.save(
                UserDevice.builder()
                        .user(user)
                        .deviceId(verifyDto.getDeviceId())
                        .deviceKey(verifyDto.getDeviceKey())
                        .deviceFingerprint(verifyDto.getDeviceFingerprint())
                        .ipAddress(verifyDto.getIp())
                        .osVersion(verifyDto.getOsVersion())
                        .appVersion(verifyDto.getAppVersion())
                        .lastLogin(OffsetDateTime.now(ZoneOffset.UTC))
                        .active(true)
                        .build()
        );
    }

//    private void manageActiveSessions(User user) {
//        List<UserSession> activeSessions = sessionService.findActiveSessionsByUserId(user.getId());
//        if (activeSessions.size() == 1) {
//            UserSession oldestSession = activeSessions.stream()
//                    .min(Comparator.comparing(UserSession::getIssuedAt))
//                    .orElse(null);
//            if (oldestSession != null) {
//                revokeSession(oldestSession.getId(), user.getId());
//            }
//        }
//    }

    private void manageActiveSessions(User user) {
        List<UserSession> activeSessions = sessionService.findActiveSessionsByUserId(user.getId());
        if (activeSessions.size() == 1) {
    //        if (activeSessions.size() >= 3) {
            UserSession oldestSession = activeSessions.stream()
                    .min(Comparator.comparing(UserSession::getIssuedAt))
                    .orElse(null);
            if (oldestSession != null) {
                revokeSession(oldestSession.getId(), user.getId());
            }
        }
    }
    /* ===========================
       تجديد التوكنات مع التحسينات الأمنية
       =========================== */
    @Transactional
    public AuthResponseDto refreshToken(RefreshRequestDto refreshDto) {
        String rawToken = refreshDto.getRefreshToken();

        try {
            // استخراج وتحقق JTI
            String jtiStr = extractAndValidateJti(rawToken);
            UUID jti = parseJti(jtiStr);

            // التحقق من معدل الطلبات
            checkRateLimit(jtiStr);

            // البحث عن التوكن في قاعدة البيانات
            RefreshToken dbToken = findRefreshToken(jti);

            // التحقق من صحة التوكن
            validateRefreshToken(rawToken, dbToken);

            // تدوير التوكنات
            AuthResponseDto response = rotateTokens(dbToken, rawToken);

            // تسجيل النجاح
            auditService.logAuthEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "SUCCESS", dbToken.getIpAddress());

            return response;
        } catch (TokenRefreshException e) {
            throw e;
        } catch (RateLimitExceededException e) {
        // Re-throw rate limit exceptions directly
        throw e;
    } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            throw new TokenRefreshException("Token refresh failed due to an unexpected error");
        }
    }

    private String extractAndValidateJti(String token) {
        try {
            String jtiStr = jwtUtil.extractJti(token);
            if (jtiStr == null || jtiStr.isBlank()) {
                log.warn("Refresh token: jti missing or unreadable");
                auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_MISSING_JTI");
                throw new TokenRefreshException("Invalid refresh token format");
            }
            return jtiStr;
        } catch (Exception ex) {
            log.warn("Refresh token: invalid token format: {}", ex.getMessage());
            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_INVALID_FORMAT");
            throw new TokenRefreshException("Invalid refresh token format");
        }
    }

    private UUID parseJti(String jtiStr) {
        try {
            return UUID.fromString(jtiStr);
        } catch (IllegalArgumentException iae) {
            log.warn("Refresh token: jti not a valid UUID: {}", jtiStr);
            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_INVALID_JTI_FORMAT");
            throw new TokenRefreshException("Invalid refresh token format");
        }
    }

    private void checkRateLimit(String jtiStr) {
        String rateLimitKey = "refresh_" + jtiStr;
        if (rateLimiterService.isRateLimited(rateLimitKey, 5, Duration.ofMinutes(1))) {
            auditService.logSecurityEvent(null, "TOKEN_REFRESH", "RATE_LIMITED");
            throw new RateLimitExceededException("Too many refresh requests");
        }
    }

    private RefreshToken findRefreshToken(UUID jti) {
        return refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found in DB: jti={}", jti);
                    auditService.logSecurityEvent(null, "TOKEN_REFRESH", "FAILED_TOKEN_NOT_FOUND");
                    return new TokenRefreshException("Invalid refresh token");
                });
    }

    private void validateRefreshToken(String rawToken, RefreshToken dbToken) {
        // التحقق من صحة JWT
        boolean jwtValid = jwtUtil.validateRefreshToken(rawToken, dbToken.getUser().getId().toString());
        if (!jwtValid) {
            log.warn("Invalid refresh token JWT: jti={}", dbToken.getJti());
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_INVALID_JWT");
            throw new TokenRefreshException("Invalid refresh token");
        }

        // التحقق من حالة التوكن
        if (dbToken.getRevokedAt() != null) {
            log.warn("Refresh token already revoked: jti={}", dbToken.getJti());
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_ALREADY_REVOKED");
            throw new TokenRefreshException("Refresh token revoked");
        }

        if (dbToken.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            log.warn("Refresh token expired in DB: jti={}", dbToken.getJti());
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REFRESH", "FAILED_EXPIRED");
            throw new TokenRefreshException("Refresh token expired");
        }

        // التحقق من hash التوكن
        String candidateHash = encryptionUtil.hash(rawToken);
        if (!secureEqualsBase64(candidateHash, dbToken.getRefreshHash())) {
            log.warn("Refresh token hash mismatch jti={}, user={}", dbToken.getJti(), dbToken.getUser().getId());
            revokeAllTokensForUser(dbToken.getUser().getId(), "Token hash mismatch detected");
            auditService.logSecurityEvent(dbToken.getUser().getId(), "TOKEN_REUSE_DETECTED", "All tokens revoked");
            throw new TokenReuseException("Token mismatch / reuse detected");
        }
    }

    // دالة مساعدة ثابتة الزمن للمقارنة (Base64-encoded digests)
    private static boolean secureEqualsBase64(String aBase64, String bBase64) {
        if (aBase64 == null || bBase64 == null) return false;
        try {
            byte[] a = Base64.getDecoder().decode(aBase64);
            byte[] b = Base64.getDecoder().decode(bBase64);
            return MessageDigest.isEqual(a, b);
        } catch (IllegalArgumentException ex) {
            // فشل في فك Base64 => لا يطابق
            return false;
        }
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

                try {
                    byte[] candidate = Base64.getDecoder().decode(candidateHash);
                    byte[] stored = Base64.getDecoder().decode(token.getRefreshHash());

                    if (!MessageDigest.isEqual(candidate, stored)) {
                        log.warn("Refresh token hash mismatch jti={}, user={}", token.getJti(), token.getUser().getId());
                        // إبطال التوكن
                        token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
                        refreshTokenRepository.save(token);

                        // استخدام الدالة المحسنة لإلغاء الجلسة والتوكنات
                        sessionService.revokeSessionAndTokensByJti(jti);

                        log.debug("Logout ok: revoked jti={}", jti);
                        auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "SUCCESS");
                    } else {
                        log.warn("Logout: token hash match? - ignoring unexpected equality (shouldn't happen)");
                        auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "FAILED_HASH_MISMATCH");
                    }
                } catch (IllegalArgumentException iae) {
                    log.warn("Logout: failed Base64 decode for candidate hash: {}", iae.getMessage());
                    auditService.logSecurityEvent(token.getUser().getId(), "LOGOUT", "FAILED_HASH_DECODE");
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
        }
    }

    private User createUserIfNotExists(String phone) {
        // نحاول استخراج E.164 أولاً ثم المخزّن
        String e164 = PhoneUtil.normalizeToE164(phone, APP_DEFAULT_REGION);
        String normalized = PhoneUtil.normalizeForStorage(phone, APP_DEFAULT_REGION);

        User user = new User();
        user.setPhoneNumber(e164 != null ? e164 : phone);
        // رمز الدولة الرقمي إن نجح الاستخراج من E.164
        String numericCountry = e164 != null ? PhoneUtil.extractCountryCodeFromE164(e164) : null;
        if (numericCountry == null) numericCountry = APP_DEFAULT_COUNTRY_NUMERIC;

        user.setCountryCode(numericCountry);
        user.setVerified(true);
        user.setNormalizedPhone(normalized);

        user.setAccountStatus(AccountStatus.ACTIVE);
        try {
            User savedUser = userRepository.save(user);

            // ملف التعريف الافتراضي
            UserProfile defaultProfile = new UserProfile();
            defaultProfile.setUser(savedUser);
            defaultProfile.setDisplayName(savedUser.getDisplayName());
            defaultProfile.setAboutText("");
            defaultProfile.setStatusEmoji("");
            defaultProfile.setLastProfileUpdate(OffsetDateTime.now());

            userProfileRepository.save(defaultProfile);
            return savedUser;
        } catch (DataIntegrityViolationException ex) {
            return userRepository.findByNormalizedPhone(normalized)
                    .orElseThrow(() -> ex);
        }
    }

    // دالة مساعدة لتوليد JTI جديدة
    public String newJti() {
        return UUID.randomUUID().toString();
    }
}
