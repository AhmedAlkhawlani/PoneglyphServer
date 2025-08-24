package com.nova.poneglyph.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

//    private final RedisTemplate<String, String> redisTemplate;
//
//    public RateLimiterService(RedisTemplate<String, String> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }
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
//    public boolean isRateLimited(String key, int maxAttempts, Duration duration) {
//        Long count = redisTemplate.opsForValue().increment(key);
//
//        if (count != null && count == 1) {
//            redisTemplate.expire(key, duration.toSeconds(), TimeUnit.SECONDS);
//        }
//
//        return count != null && count > maxAttempts;
//    }
//    public boolean isRateLimited(String key, int maxAttempts, Duration duration) {
//        ValueOperations<String, String> ops = redisTemplate.opsForValue();
//        String currentAttempts = ops.get(key);
//
//        if (currentAttempts == null) {
//            ops.set(key, "1", duration.toSeconds(), TimeUnit.SECONDS);
//            return false;
//        }
//
//        int attempts = Integer.parseInt(currentAttempts);
//        if (attempts >= maxAttempts) {
//            return true;
//        }
//
//        ops.increment(key);
//        return false;
//    }

    public long getRemainingTime(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void resetRateLimit(String key) {
        redisTemplate.delete(key);
    }
}
