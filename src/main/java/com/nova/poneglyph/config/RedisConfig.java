//package com.nova.poneglyph.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.cache.RedisCacheWriter;
//import org.springframework.data.redis.connection.RedisClusterConfiguration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//import java.util.List;
//
//@Configuration
//@EnableCaching
//public class RedisConfig {
//
//    // Redis Cluster nodes (يمكن تعديلها حسب السيرفر)
//    @Value("${redis.cluster.nodes:localhost:6379}")
//    private String clusterNodes;
//
//    // TTL افتراضي للكاش
//    @Value("${redis.cache.ttl:3600}") // بالثواني (هنا ساعة)
//    private long defaultTTL;
//
//    /**
//     * إنشاء اتصال Redis (Cluster أو Standalone)
//     */
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        if (clusterNodes.contains(",")) { // إذا كان Cluster
//            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(List.of(clusterNodes.split(",")));
//            return new LettuceConnectionFactory(clusterConfig);
//        } else { // Standalone
//            String[] hostPort = clusterNodes.split(":");
//            String host = hostPort[0];
//            int port = Integer.parseInt(hostPort[1]);
//            return new LettuceConnectionFactory(host, port);
//        }
//    }
//
//    /**
//     * StringRedisTemplate جاهز للاستخدام مع النصوص
//     */
//    @Bean
//    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
//        StringRedisTemplate template = new StringRedisTemplate();
//        template.setConnectionFactory(connectionFactory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new StringRedisSerializer());
//        template.afterPropertiesSet();
//        return template;
//    }
//
//    /**
//     * CacheManager للاستفادة من @Cacheable و TTL تلقائي
//     */
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
//        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofSeconds(defaultTTL))
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
//
//        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
//                .cacheDefaults(config)
//                .build();
//    }
//}
