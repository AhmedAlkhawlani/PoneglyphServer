//package com.nova.poneglyph.config.websocket;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.nova.poneglyph.config.v2.TokenBlacklistService;
//import com.nova.poneglyph.exception.AuthenticationException;
//import com.nova.poneglyph.util.JwtUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//@Component
//public class WebSocketAuthInterceptor implements ChannelInterceptor {
//
//    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
//
//    private final JwtUtil jwtUtil;
//    private final TokenBlacklistService tokenBlacklistService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public WebSocketAuthInterceptor(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
//        this.jwtUtil = jwtUtil;
//        this.tokenBlacklistService = tokenBlacklistService;
//    }
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
//                message, StompHeaderAccessor.class
//        );
//
//        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
//            try {
//                String token = extractTokenFromRequest(accessor, message);
//
//                if (token == null || token.isEmpty()) {
//                    log.warn("No authorization token found for WebSocket connection");
//                    throw new AuthenticationException("Authorization token required");
//                }
//
//                authenticateToken(token, accessor);
//            } catch (AuthenticationException e) {
//                log.warn("WebSocket authentication failed: {}", e.getMessage());
//                throw e;
//            } catch (Exception e) {
//                log.error("Unexpected error during WebSocket authentication: {}", e.getMessage());
//                throw new AuthenticationException("Authentication failed");
//            }
//        }
//
//        return message;
//    }
//
//    private String extractTokenFromRequest(StompHeaderAccessor accessor, Message<?> message) {
//        try {
//            // المحاولة الأولى: الحصول من headers
//            List<String> authHeaders = accessor.getNativeHeader("Authorization");
//            if (authHeaders != null && !authHeaders.isEmpty()) {
//                String headerValue = authHeaders.get(0);
//                if (headerValue.startsWith("Bearer ")) {
//                    return headerValue.substring(7).trim();
//                }
//                return headerValue.trim();
//            }
//
//            // المحاولة الثانية: الحصول من query parameters
//            String query = accessor.getFirstNativeHeader("query");
//            if (query != null && query.contains("token=")) {
//                String[] params = query.split("&");
//                for (String param : params) {
//                    if (param.startsWith("token=")) {
//                        return param.substring(6);
//                    }
//                }
//            }
//
//            // المحاولة الثالثة: الحصول من payload
//            Object payload = message.getPayload();
//            if (payload instanceof String) {
//                try {
//                    Map<String, Object> payloadMap = objectMapper.readValue((String) payload, Map.class);
//                    if (payloadMap.containsKey("token")) {
//                        return payloadMap.get("token").toString();
//                    }
//                    if (payloadMap.containsKey("type") && "AUTH".equals(payloadMap.get("type")) && payloadMap.containsKey("token")) {
//                        return payloadMap.get("token").toString();
//                    }
//                } catch (Exception e) {
//                    log.debug("Failed to parse WebSocket payload: {}", e.getMessage());
//                }
//            }
//
//            return null;
//        } catch (Exception e) {
//            log.error("Error extracting token from request: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    private void authenticateToken(String token, StompHeaderAccessor accessor) {
//        // التحقق من أن التوكن غير ملغى
//        if (tokenBlacklistService.isTokenBlacklisted(token)) {
//            log.warn("Blacklisted token attempt for WebSocket connection");
//            throw new AuthenticationException("Token has been revoked");
//        }
//
//        // استخراج userId من التوكن
//        String userIdStr = jwtUtil.extractUserId(token);
//        if (userIdStr == null) {
//            log.warn("Failed to extract user ID from JWT token");
//            throw new AuthenticationException("Invalid authentication token");
//        }
//
//        // التحقق من صحة التوكن
//        boolean isValid = jwtUtil.validateAccessToken(token, userIdStr);
//        if (!isValid) {
//            log.warn("Invalid JWT token for WebSocket connection");
//            throw new AuthenticationException("Invalid authentication token");
//        }
//
//        try {
//            UUID userId = UUID.fromString(userIdStr);
//            UsernamePasswordAuthenticationToken auth =
//                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
//            SecurityContextHolder.getContext().setAuthentication(auth);
//            accessor.setUser(auth);
//
//            log.debug("WebSocket authentication successful for user: {}", userId);
//        } catch (IllegalArgumentException e) {
//            log.warn("Invalid user ID format in JWT token: {}", userIdStr);
//            throw new AuthenticationException("Invalid user ID format");
//        }
//    }
//
//    @Override
//    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
//        // تنظيف SecurityContext بعد إرسال الرسالة
//        SecurityContextHolder.clearContext();
//    }
//
//    @Override
//    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
//        // تنظيف إضافي إذا لزم الأمر
//        SecurityContextHolder.clearContext();
//    }
//}
package com.nova.poneglyph.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.poneglyph.config.v2.TokenBlacklistService;
import com.nova.poneglyph.exception.AuthenticationException;
import com.nova.poneglyph.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketAuthInterceptor(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class
        );

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                String token = extractTokenFromRequest(accessor);

                if (token == null || token.isEmpty()) {
                    log.warn("No authorization token found for WebSocket connection (Authorization header required)");
                    throw new AuthenticationException("Authorization token required");
                }

                authenticateToken(token, accessor);
            } catch (AuthenticationException e) {
                log.warn("WebSocket authentication failed: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Unexpected error during WebSocket authentication: {}", e.getMessage());
                throw new AuthenticationException("Authentication failed");
            }
        }

        return message;
    }

    /**
     * Accept only Authorization header (Bearer ...) from STOMP headers.
     * DO NOT parse token from query string or payload to avoid logging/leakage risks.
     */
    private String extractTokenFromRequest(StompHeaderAccessor accessor) {
        try {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String headerValue = authHeaders.get(0);
                if (headerValue.startsWith("Bearer ")) {
                    return headerValue.substring(7).trim();
                }
                return headerValue.trim();
            }
            // If Authorization header is missing, do NOT fallback to query/payload
            return null;
        } catch (Exception e) {
            log.error("Error extracting token from websocket headers: {}", e.getMessage());
            return null;
        }
    }

    private void authenticateToken(String token, StompHeaderAccessor accessor) {
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            log.warn("Blacklisted token attempt for WebSocket connection");
            throw new AuthenticationException("Token has been revoked");
        }

        String userIdStr = jwtUtil.extractUserId(token);
        if (userIdStr == null) {
            log.warn("Failed to extract user ID from JWT token");
            throw new AuthenticationException("Invalid authentication token");
        }

        boolean isValid = jwtUtil.validateAccessToken(token, userIdStr);
        if (!isValid) {
            log.warn("Invalid JWT token for WebSocket connection");
            throw new AuthenticationException("Invalid authentication token");
        }

        try {
            UUID userId = UUID.fromString(userIdStr);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
            accessor.setUser(auth);

            log.debug("WebSocket authentication successful for user: {}", userId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user ID format in JWT token: {}", userIdStr);
            throw new AuthenticationException("Invalid user ID format");
        }
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        SecurityContextHolder.clearContext();
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        SecurityContextHolder.clearContext();
    }
}
