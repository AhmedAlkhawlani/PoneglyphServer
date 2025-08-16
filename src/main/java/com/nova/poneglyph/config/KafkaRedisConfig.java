package com.nova.poneglyph.config;



import com.nova.poneglyph.events.NotificationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

@Configuration
public class KafkaRedisConfig {

    /**
     * إنشاء اتصال Redis
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    /**
     * StringRedisTemplate مع Serializers جاهز للاستخدام
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * إعداد TTL افتراضي لكل نوع إشعار
     */
    @Bean
    public Map<NotificationEvent.EventType, Duration> notificationTTLs() {
        Map<NotificationEvent.EventType, Duration> ttls = new EnumMap<>(NotificationEvent.EventType.class);
        ttls.put(NotificationEvent.EventType.MESSAGE, Duration.ofMinutes(5));
        ttls.put(NotificationEvent.EventType.DELIVERY, Duration.ofMinutes(5));
        ttls.put(NotificationEvent.EventType.SEEN, Duration.ofMinutes(5));
        ttls.put(NotificationEvent.EventType.CONVERSATION_UPDATE, Duration.ofMinutes(10));
        ttls.put(NotificationEvent.EventType.PRESENCE, Duration.ofMinutes(1));
        ttls.put(NotificationEvent.EventType.TYPING, Duration.ofMinutes(1));
        ttls.put(NotificationEvent.EventType.SYSTEM, Duration.ofDays(1));
        ttls.put(NotificationEvent.EventType.ALERT, Duration.ofDays(1));
        return ttls;
    }
}
