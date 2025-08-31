package com.nova.poneglyph.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {


    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimiterService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    // في RateLimiterService - التصميم الحالي جيد ولكن يمكن تحسينه
    public boolean isRateLimited(String key, int maxAttempts, Duration duration) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Long count = ops.increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, duration.toSeconds(), TimeUnit.SECONDS);
        }

        return count != null && count > maxAttempts;
    }


    public long getRemainingTime(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void resetRateLimit(String key) {
        redisTemplate.delete(key);
    }
}
