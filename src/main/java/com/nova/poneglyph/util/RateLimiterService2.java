// File: src/main/java/com/nova/poneglyph/util/RateLimiterService.java
package com.nova.poneglyph.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter (per-JVM). For production use a distributed store (Redis/Bucket4j).
 */
@Service
public class RateLimiterService2 {
    private static final Logger log = LoggerFactory.getLogger(RateLimiterService2.class);

    private static class Counter {
        AtomicInteger count = new AtomicInteger(0);
        Instant windowStart = Instant.now();
    }

    private final Map<String, Counter> map = new ConcurrentHashMap<>();

    /**
     * Returns true if the key is rate-limited (exceeded maxRequests within window)
     */
    public boolean isRateLimited(String key, int maxRequests, Duration window) {
        Counter c = map.computeIfAbsent(key, k -> new Counter());
        synchronized (c) {
            Instant now = Instant.now();
            if (now.isAfter(c.windowStart.plus(window))) {
                // reset
                c.count.set(0);
                c.windowStart = now;
            }
            int current = c.count.incrementAndGet();
            boolean limited = current > maxRequests;
            if (limited) {
                log.warn("Rate limit exceeded for key={} (count={} max={})", key, current, maxRequests);
            }
            return limited;
        }
    }

    public void reset(String key) {
        map.remove(key);
    }
}
