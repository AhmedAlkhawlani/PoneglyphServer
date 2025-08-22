package com.nova.poneglyph.config.websocket;


import com.nova.poneglyph.util.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
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

    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class
        );

        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String token = authHeaders.get(0).replace("Bearer ", "");
//            if (jwtUtil.validateToken(token)) {
//                UUID userId = jwtUtil.extractUserId(token);
//                UsernamePasswordAuthenticationToken auth =
//                        new UsernamePasswordAuthenticationToken(userId, null, List.of());
//                SecurityContextHolder.getContext().setAuthentication(auth);
//                accessor.setUser(auth);
//            }
        }
        return message;
    }
}
