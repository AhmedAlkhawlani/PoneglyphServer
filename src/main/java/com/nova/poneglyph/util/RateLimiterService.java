package com.nova.poneglyph.util;



import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;

    public RateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isRateLimited(String key, int maxAttempts, Duration duration) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String currentAttempts = ops.get(key);

        if (currentAttempts == null) {
            ops.set(key, "1", duration.toSeconds(), TimeUnit.SECONDS);
            return false;
        }

        int attempts = Integer.parseInt(currentAttempts);
        if (attempts >= maxAttempts) {
            return true;
        }

        ops.increment(key);
        return false;
    }

    public long getRemainingTime(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void resetRateLimit(String key) {
        redisTemplate.delete(key);
    }
}
