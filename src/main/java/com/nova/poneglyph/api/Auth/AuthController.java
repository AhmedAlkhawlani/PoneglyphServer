////package com.nova.poneglyph.api.Auth;
////
////
////
////import com.nova.poneglyph.dto.authDto.AuthResponseDto;
////import com.nova.poneglyph.dto.authDto.OtpRequestDto;
////import com.nova.poneglyph.dto.authDto.OtpVerifyDto;
////import com.nova.poneglyph.dto.authDto.RefreshRequestDto;
////import com.nova.poneglyph.service.UserGuardService;
////import com.nova.poneglyph.service.audit.AuditService;
////import com.nova.poneglyph.service.auth.AuthService;
////import jakarta.servlet.http.HttpServletRequest;
////import jakarta.validation.Valid;
////import lombok.RequiredArgsConstructor;
////import org.springframework.http.ResponseEntity;
////import org.springframework.security.core.annotation.AuthenticationPrincipal;
////import org.springframework.web.bind.annotation.*;
////import java.util.UUID;
////
////@RestController
////@RequestMapping("/api/auth")
////@RequiredArgsConstructor
////public class AuthController {
////
////    private final AuthService authService;
////    private final UserGuardService userGuardService;
////    private final AuditService auditService;
////
////    @PostMapping("/otp/request")
////    public ResponseEntity<?> requestOtp(
////            @RequestBody @Valid OtpRequestDto requestDto,
////            HttpServletRequest servletRequest) {
////
////        // التحقق من إمكانية إنشاء طلب OTP
////        userGuardService.canCreateAccount(requestDto.getPhone());
////
////        String clientIp = getClientIp(servletRequest);
////        requestDto.setIp(clientIp);
////
////        authService.requestOtp(requestDto);
////
////        // تسجيل حدث التدقيق
////        auditService.logAuthEvent(null, "OTP_REQUEST", "SENT", clientIp);
////
////        return ResponseEntity.ok().build();
////    }
////
////    @PostMapping("/otp/verify")
////    public ResponseEntity<AuthResponseDto> verifyOtp(
////            @RequestBody @Valid OtpVerifyDto verifyDto,
////            HttpServletRequest servletRequest) {
////
////        String clientIp = getClientIp(servletRequest);
////        verifyDto.setIp(clientIp);
////
////        AuthResponseDto response = authService.verifyOtp(verifyDto);
////
////        // تسجيل حدث التدقيق
////        auditService.logAuthEvent(
////                UUID.fromString(response.getUserId()),
////                "OTP_VERIFY",
////                "SUCCESS",
////                clientIp
////        );
////
////        // إعادة تعيين عداد المحاولات الفاشلة
////        userGuardService.resetFailedLoginAttempts(UUID.fromString(response.getUserId()));
////
////        return ResponseEntity.ok(response);
////    }
////
////    @PostMapping("/token/refresh")
////    public ResponseEntity<AuthResponseDto> refreshToken(
////            @RequestBody @Valid RefreshRequestDto refreshDto) {
////
////        AuthResponseDto response = authService.refreshToken(refreshDto);
////        return ResponseEntity.ok(response);
////    }
////
////    @PostMapping("/logout")
////    public ResponseEntity<?> logout(
////            @RequestBody @Valid RefreshRequestDto refreshDto) {
////
////        authService.logout(refreshDto.getRefreshToken());
////        return ResponseEntity.ok().build();
////    }
////
////    @PostMapping("/sessions/revoke-all")
////    public ResponseEntity<?> revokeAllSessions(
////             @AuthenticationPrincipal CustomUserDetails userDetails) {
////
////        authService.revokeAllSessions(userId);
////        return ResponseEntity.ok().build();
////    }
////
////    private String getClientIp(HttpServletRequest request) {
////        String ip = request.getHeader("X-Forwarded-For");
////        if (ip == null || ip.isEmpty()) {
////            return request.getRemoteAddr();
////        }
////        return ip.split(",")[0];
////    }
////}
//
//package com.nova.poneglyph.api.Auth;
//
//import com.nova.poneglyph.dto.authDto.AuthResponseDto;
//import com.nova.poneglyph.dto.authDto.OtpRequestDto;
//import com.nova.poneglyph.dto.authDto.OtpVerifyDto;
//import com.nova.poneglyph.dto.authDto.RefreshRequestDto;
//import com.nova.poneglyph.service.UserGuardService;
//import com.nova.poneglyph.service.auth.AuthService;
//import com.nova.poneglyph.service.audit.AuditService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final AuthService authService;
//    private final UserGuardService userGuardService;
//    private final AuditService auditService;
//
//    @PostMapping("/otp/request")
//    public ResponseEntity<?> requestOtp(
//            @RequestBody @Valid OtpRequestDto requestDto,
//            HttpServletRequest servletRequest) {
//
//        // التحقق من إمكانية إنشاء طلب OTP
//        userGuardService.canCreateAccount(requestDto.getPhone());
//
//        String clientIp = getClientIp(servletRequest);
//        requestDto.setIp(clientIp);
//
//        authService.requestOtp(requestDto);
//
//        // تسجيل حدث التدقيق
//        auditService.logAuthEvent(null, "OTP_REQUEST", "SENT", clientIp);
//
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/otp/verify")
//    public ResponseEntity<AuthResponseDto> verifyOtp(
//            @RequestBody @Valid OtpVerifyDto verifyDto,
//            HttpServletRequest servletRequest) {
//
//        String clientIp = getClientIp(servletRequest);
//        verifyDto.setIp(clientIp);
//
//        AuthResponseDto response = authService.verifyOtp(verifyDto);
//
//        // تسجيل حدث التدقيق
//        auditService.logAuthEvent(
//                UUID.fromString(response.getUserId()),
//                "OTP_VERIFY",
//                "SUCCESS",
//                clientIp
//        );
//
//        // إعادة تعيين عداد المحاولات الفاشلة
//        userGuardService.resetFailedLoginAttempts(UUID.fromString(response.getUserId()));
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/token/refresh")
//    public ResponseEntity<AuthResponseDto> refreshToken(
//            @RequestBody @Valid RefreshRequestDto refreshDto) {
//
//        AuthResponseDto response = authService.refreshToken(refreshDto);
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<?> logout(
//            @RequestBody @Valid RefreshRequestDto refreshDto) {
//
//        authService.logout(refreshDto.getRefreshToken());
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/sessions/revoke-all")
//    public ResponseEntity<?> revokeAllSessions(
//             @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        authService.revokeAllSessions(userId);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/sessions")
//    public ResponseEntity<?> getActiveSessions(
//             @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        return ResponseEntity.ok(authService.getActiveSessions(userId));
//    }
//
//    @DeleteMapping("/sessions/{sessionId}")
//    public ResponseEntity<?> revokeSession(
//            @PathVariable UUID sessionId,
//             @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        authService.revokeSession(sessionId, userId);
//        return ResponseEntity.ok().build();
//    }
//
//    private String getClientIp(HttpServletRequest request) {
//        String ip = request.getHeader("X-Forwarded-For");
//        if (ip == null || ip.isEmpty()) {
//            return request.getRemoteAddr();
//        }
//        return ip.split(",")[0];
//    }
//
//}

package com.nova.poneglyph.api.Auth;

import com.nova.poneglyph.config.v2.CustomUserDetails;
import com.nova.poneglyph.dto.authDto.*;
import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.dto.userDto.UserSessionDto;
import com.nova.poneglyph.exception.*;
import com.nova.poneglyph.service.UserGuardService;
import com.nova.poneglyph.service.auth.AuthService;
import com.nova.poneglyph.service.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final UserGuardService userGuardService;
    private final AuditService auditService;

    @PostMapping("/otp/request")
    public ResponseEntity<?> requestOtp(
            @RequestBody @Valid OtpRequestDto requestDto,
            HttpServletRequest servletRequest) {

        userGuardService.canCreateAccount(requestDto.getPhone());

        String clientIp = getClientIp(servletRequest);
        requestDto.setIp(clientIp);

        authService.requestOtp(requestDto);

        auditService.logAuthEvent(null, "OTP_REQUEST", "SENT", clientIp);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponseDto> verifyOtp(
            @RequestBody @Valid OtpVerifyDto verifyDto,
            HttpServletRequest servletRequest) {

        String clientIp = getClientIp(servletRequest);
        verifyDto.setIp(clientIp);

        AuthResponseDto response = authService.verifyOtp(verifyDto);

        try {
            UUID userId = UUID.fromString(response.getUserId());
            auditService.logAuthEvent(userId, "OTP_VERIFY", "SUCCESS", clientIp);
            userGuardService.resetFailedLoginAttempts(userId);
        } catch (Exception ex) {
            log.warn("Failed to parse userId from AuthResponseDto: {}", ex.getMessage());
            auditService.logAuthEvent(null, "OTP_VERIFY", "SUCCESS", clientIp);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(
            @RequestBody @Valid RefreshRequestDto refreshDto,
            HttpServletRequest servletRequest) {

        String ip = getClientIp(servletRequest);

        AuthResponseDto response = authService.refreshToken(refreshDto);

        try {
            UUID userId = UUID.fromString(response.getUserId());
            auditService.logAuthEvent(userId, "TOKEN_REFRESH", "SUCCESS", ip);
        } catch (Exception ex) {
            auditService.logAuthEvent(null, "TOKEN_REFRESH", "SUCCESS", ip);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody @Valid RefreshRequestDto refreshDto,
            HttpServletRequest servletRequest) {

        String ip = getClientIp(servletRequest);

        authService.logout(refreshDto.getRefreshToken());

        auditService.logAuthEvent(null, "LOGOUT", "SUCCESS", ip);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sessions/revoke-all")
    public ResponseEntity<?> revokeAllSessions(
             @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {
        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        if (userId == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        authService.revokeAllSessions(userId);

        auditService.logAuthEvent(userId, "REVOKE_ALL_SESSIONS", "SUCCESS", getClientIp(servletRequest));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<UserSessionDto>> getActiveSessions( @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        if (userId == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        List<UserSessionDto> sessions = authService.getActiveSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> revokeSession(
            @PathVariable UUID sessionId,
             @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        if (userId == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        authService.revokeSession(sessionId, userId);

        auditService.logAuthEvent(userId, "REVOKE_SESSION", "SUCCESS", getClientIp(servletRequest));
        return ResponseEntity.ok().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP", "True-Client-IP"};
        for (String h : headers) {
            String ip = request.getHeader(h);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }


}

