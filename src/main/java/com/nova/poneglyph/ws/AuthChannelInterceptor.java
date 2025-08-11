//package com.nova.poneglyph.ws;
//
//import com.nova.poneglyph.config.PresenceNotifier;
//import com.nova.poneglyph.utils.JWTUtils;
//import com.nova.poneglyph.service.CustomUserDetailsService;
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
//    private final CustomUserDetailsService customUserDetailsService;
//    @Lazy
//    private final PresenceNotifier presenceNotifier; // <-- استخدام الواجهة بدلاً من PresenceService
//
//    // حقن عبر Constructor بدون PresenceService
//
//    public AuthChannelInterceptor(JWTUtils jwtUtils,
//                                  CustomUserDetailsService customUserDetailsService, PresenceNotifier presenceNotifier) {
//        this.jwtUtils = jwtUtils;
//        this.customUserDetailsService = customUserDetailsService;
//        this.presenceNotifier = presenceNotifier;
//    }
//    // حقن PresenceService لاحقاً عبر Setter
//
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//        if (accessor != null) {
//            // تحديث آخر نشاط لأي رسالة واردة
//            // تحديث آخر نشاط لأي رسالة واردة
//            Authentication authentication = (Authentication) accessor.getUser();
//            if (authentication != null) {
//                presenceNotifier.updateLastActivity(authentication.getName());
//
//                // معالجة النبضات القلبية
////                if (StompCommand.HEARTBEAT.equals(accessor.getCommand())) {
////                    presenceNotifier.handleHeartbeat(authentication.getName());
////                }
//            }
//            // التعامل مع CONNECT frames
//            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                handleConnect(accessor);
//            }
//            // التعامل مع DISCONNECT frames
//            else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
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
//                // تحديث حالة الاتصال عند الاتصال
//                presenceNotifier.handleConnectionEvent(authentication.getName(), true);
//            }
//        }
//    }
//
//    private void handleDisconnect(StompHeaderAccessor accessor) {
//        Authentication authentication = (Authentication) accessor.getUser();
//        if (authentication != null) {
//            // تحديث حالة الاتصال عند الانقطاع
//            presenceNotifier.handleConnectionEvent(authentication.getName(), false);
//        }
//    }
//
////    @Override
////    public Message<?> preSend(Message<?> message, MessageChannel channel) {
////        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
////
////        // التعامل مع CONNECT frames
////        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
////            String authToken = accessor.getFirstNativeHeader("Authorization");
////
////            if (authToken != null && authToken.startsWith("Bearer ")) {
////                String token = authToken.substring(7);
////                Authentication authentication = validateTokenAndCreateAuthentication(token);
////                if (authentication != null) {
////                    accessor.setUser(authentication);
////                }
////            }
////        }
////
////        return message;
////    }
//
//    private Authentication validateTokenAndCreateAuthentication(String token) {
//        String username = getUsernameFromToken(token);
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
//
//    private String getUsernameFromToken(String token) {
//        return jwtUtils.extractUsername(token);
//    }
//}

package com.nova.poneglyph.ws;

import com.nova.poneglyph.config.PresenceNotifier;
import com.nova.poneglyph.service.CustomUserDetailsService;
import com.nova.poneglyph.utils.JWTUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JWTUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;
    @Lazy
    private final PresenceNotifier presenceNotifier;

    public AuthChannelInterceptor(JWTUtils jwtUtils,
                                  CustomUserDetailsService customUserDetailsService,
                                  PresenceNotifier presenceNotifier) {
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
        this.presenceNotifier = presenceNotifier;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            Authentication authentication = (Authentication) accessor.getUser();

            // أي رسالة من العميل تعتبر نبضة قلب
            if (authentication != null) {
                presenceNotifier.handleHeartbeat(authentication.getName());
            }

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                handleConnect(accessor);
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                handleDisconnect(accessor);
            }
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authToken = accessor.getFirstNativeHeader("Authorization");

        if (authToken != null && authToken.startsWith("Bearer ")) {
            String token = authToken.substring(7);
            Authentication authentication = validateTokenAndCreateAuthentication(token);
            if (authentication != null) {
                accessor.setUser(authentication);
                presenceNotifier.handleConnectionEvent(authentication.getName(), true);
            }
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        Authentication authentication = (Authentication) accessor.getUser();
        if (authentication != null) {
            presenceNotifier.handleConnectionEvent(authentication.getName(), false);
        }
    }

    private Authentication validateTokenAndCreateAuthentication(String token) {
        String username = jwtUtils.extractUsername(token);
        if (username == null) {
            return null;
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        if (jwtUtils.isValidToken(token, userDetails)) {
            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
        }
        return null;
    }
}
