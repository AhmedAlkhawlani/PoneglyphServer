package com.nova.poneglyph.config.v2;



import com.nova.poneglyph.util.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RiskAnalysisService {

    private final RateLimiterService rateLimiterService;
    private final Map<String, Integer> riskScores = new HashMap<>();

    public int calculateRiskScore(String ip, String deviceId, String phone) {
        int score = 0;

        // تحليل基于معدل الطلبات
        if (rateLimiterService.isRateLimited("risk:" + ip, 10, Duration.ofMinutes(5))) {
            score += 30;
        }

        // تحليل基于معدل الطلبات للجهاز
        if (rateLimiterService.isRateLimited("risk_device:" + deviceId, 5, Duration.ofMinutes(10))) {
            score += 20;
        }

        // تحليل基于معدل الطلبات للرقم
        if (rateLimiterService.isRateLimited("risk_phone:" + phone, 3, Duration.ofMinutes(15))) {
            score += 25;
        }

        // تحليل基于الموقع (يمكن إضافة تحقق من IP geolocation)
        if (isSuspiciousIp(ip)) {
            score += 15;
        }

        riskScores.put(ip + ":" + deviceId, score);
        return score;
    }

    public boolean requiresAdditionalVerification(int riskScore) {
        return riskScore > 50;
    }

    public boolean shouldBlockRequest(int riskScore) {
        return riskScore > 80;
    }

    private boolean isSuspiciousIp(String ip) {
        // تحقق مبسط من عناوين IP المشبوهة
        return ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172.16.") ||
                ip.equals("127.0.0.1");
    }

    public int getRiskScore(String ip, String deviceId) {
        return riskScores.getOrDefault(ip + ":" + deviceId, 0);
    }
}
