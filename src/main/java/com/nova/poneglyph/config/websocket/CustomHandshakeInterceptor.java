package com.nova.poneglyph.config.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
@Component

public class CustomHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger log = LoggerFactory.getLogger(CustomHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // نسخ التوكن من query param إلى attributes لتستخدمه لاحقاً في ChannelInterceptor إذا لم يرسل العميل header
        try {
            if (request instanceof ServletServerHttpRequest servletReq) {
                String token = servletReq.getServletRequest().getParameter("token");
                if (token != null && !token.isBlank()) {
                    attributes.put("ws_token_query", token);
                    log.debug("CustomHandshakeInterceptor: found token in query and saved to session attributes");
                }
            }
        } catch (Exception e) {
            log.debug("CustomHandshakeInterceptor error reading query token: {}", e.getMessage());
        }
        // السماح بكل المصافحات (مقبولة عندك)
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // لا شيء
    }
}
