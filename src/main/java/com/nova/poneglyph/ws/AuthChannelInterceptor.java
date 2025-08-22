//
//
//package com.nova.poneglyph.ws;
//
//import com.nova.poneglyph.config.PresenceNotifier;
////import com.nova.poneglyph.service.CustomUserDetailsService;
//import com.nova.poneglyph.utils.JWTUtils;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.simp.stomp.StompCommand;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.support.ChannelInterceptor;
//import org.springframework.messaging.support.MessageHeaderAccessor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//@Component
//public class AuthChannelInterceptor implements ChannelInterceptor {
//
//    private final JWTUtils jwtUtils;
////    private final CustomUserDetailsService customUserDetailsService;
//    @Lazy
//    private final PresenceNotifier presenceNotifier;
//
//    public AuthChannelInterceptor(JWTUtils jwtUtils,
//                                  CustomUserDetailsService customUserDetailsService,
//                                  PresenceNotifier presenceNotifier) {
//        this.jwtUtils = jwtUtils;
//        this.customUserDetailsService = customUserDetailsService;
//        this.presenceNotifier = presenceNotifier;
//    }
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor =
//                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//        if (accessor != null) {
//            Authentication authentication = (Authentication) accessor.getUser();
//
//            // أي رسالة من العميل تعتبر نبضة قلب
//            if (authentication != null) {
//                presenceNotifier.handleHeartbeat(authentication.getName());
//            }
//
//            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                handleConnect(accessor);
//            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
//                handleDisconnect(accessor);
//            }
//        }
//
//        return message;
//    }
//
//    private void handleConnect(StompHeaderAccessor accessor) {
//        String authToken = accessor.getFirstNativeHeader("Authorization");
//
//        if (authToken != null && authToken.startsWith("Bearer ")) {
//            String token = authToken.substring(7);
//            Authentication authentication = validateTokenAndCreateAuthentication(token);
//            if (authentication != null) {
//                accessor.setUser(authentication);
//                presenceNotifier.handleConnectionEvent(authentication.getName(), true);
//            }
//        }
//    }
//
//    private void handleDisconnect(StompHeaderAccessor accessor) {
//        Authentication authentication = (Authentication) accessor.getUser();
//        if (authentication != null) {
//            presenceNotifier.handleConnectionEvent(authentication.getName(), false);
//        }
//    }
//
//    private Authentication validateTokenAndCreateAuthentication(String token) {
//        String username = jwtUtils.extractUsername(token);
//        if (username == null) {
//            return null;
//        }
//
//        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
//        if (jwtUtils.isValidToken(token, userDetails)) {
//            return new UsernamePasswordAuthenticationToken(
//                    userDetails,
//                    null,
//                    userDetails.getAuthorities()
//            );
//        }
//        return null;
//    }
//}
