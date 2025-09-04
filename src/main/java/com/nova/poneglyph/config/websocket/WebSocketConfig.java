package com.nova.poneglyph.config.websocket;

import com.nova.poneglyph.config.v2.TokenBlacklistService;
import com.nova.poneglyph.util.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final CustomHandshakeInterceptor handshakeInterceptor;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final CustomHandshakeHandler handshakeHandler;

    public WebSocketConfig(JwtUtil jwtUtil,
                           TokenBlacklistService tokenBlacklistService,
                           CustomHandshakeInterceptor handshakeInterceptor,
                           WebSocketAuthInterceptor webSocketAuthInterceptor,
                           CustomHandshakeHandler handshakeHandler) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.handshakeInterceptor = handshakeInterceptor;
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
        this.handshakeHandler = handshakeHandler;
    }

//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
//        taskScheduler.setPoolSize(1);
//        taskScheduler.setThreadNamePrefix("wss-heartbeat-thread-");
//        taskScheduler.initialize();
//
////        config.enableSimpleBroker("/topic", "/queue", "/user")
////                .setHeartbeatValue(new long[]{10000, 10000})
////                .setTaskScheduler(taskScheduler);
//
//        config.enableSimpleBroker("/topic", "/queue", "/user")
//                .setHeartbeatValue(new long[]{25000, 25000}) // 25s for both directions
//                .setTaskScheduler(taskScheduler);
//
//        config.setApplicationDestinationPrefixes("/app");
//        config.setUserDestinationPrefix("/user");
//        config.setPreservePublishOrder(true);
//    }
@Override
public void configureMessageBroker(MessageBrokerRegistry config) {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(4); // زيادة عدد threads لمعالجة الرسائل
    taskScheduler.setThreadNamePrefix("wss-heartbeat-thread-");
    taskScheduler.initialize();

    config.enableSimpleBroker("/topic", "/queue", "/user")
            .setHeartbeatValue(new long[]{10000, 10000}) // تقليل وقت الheartbeat
            .setTaskScheduler(taskScheduler);

    config.setApplicationDestinationPrefixes("/app");
    config.setUserDestinationPrefix("/user");
    config.setPreservePublishOrder(true);
}

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
        registration.taskExecutor()
                .corePoolSize(20) // زيادة حجم pool للإدخال
                .maxPoolSize(40)
                .queueCapacity(2000); // زيادة سعة الطابور
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.taskExecutor()
                .corePoolSize(20) // زيادة حجم pool للإخراج
                .maxPoolSize(40)
                .queueCapacity(2000); // زيادة سعة الطابور
    }
//
//// في WebSocketConfig
//@Override
//public void configureMessageBroker(MessageBrokerRegistry config) {
//    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
//    taskScheduler.setPoolSize(2); // زيادة عدد threads لمعالجة الرسائل
//    taskScheduler.setThreadNamePrefix("wss-heartbeat-thread-");
//    taskScheduler.initialize();
//
//    config.enableSimpleBroker("/topic", "/queue", "/user")
//            .setHeartbeatValue(new long[]{10000, 10000}) // تقليل وقت الheartbeat
//            .setTaskScheduler(taskScheduler);
//
//    config.setApplicationDestinationPrefixes("/app");
//    config.setUserDestinationPrefix("/user");
//    config.setPreservePublishOrder(true);
//}
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor)
                .setHandshakeHandler(handshakeHandler)
                .withSockJS()
                .setHeartbeatTime(25000)
                .setSessionCookieNeeded(false);

        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor)
                .setHandshakeHandler(handshakeHandler);
    }
//
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(webSocketAuthInterceptor);
//        registration.taskExecutor()
//                .corePoolSize(10)
//                .maxPoolSize(20)
//                .queueCapacity(1000);
//    }
//
//    @Override
//    public void configureClientOutboundChannel(ChannelRegistration registration) {
//        registration.taskExecutor()
//                .corePoolSize(10)
//                .maxPoolSize(20)
//                .queueCapacity(1000);
//    }
}
