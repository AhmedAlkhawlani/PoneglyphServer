package com.nova.poneglyph.service.audit;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.poneglyph.domain.audit.AuditLog;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.repository.AuditLogRepository;
import com.nova.poneglyph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * تسجيل حدث تدقيق عام
     */
    @Transactional
    public void logEvent(UUID actorId, String action, String targetType, String targetId, Map<String, Object> metadata) {
        AuditLog auditLog = new AuditLog();

        if (actorId != null) {
            User actor = userRepository.findById(actorId).orElse(null);
            auditLog.setActor(actor);
        }

        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);

        if (metadata != null && !metadata.isEmpty()) {
            auditLog.setMetadata(convertMapToJson(metadata));
        }

        auditLog.setCreatedAt(OffsetDateTime.now());
        auditLogRepository.save(auditLog);
    }

    /**
     * تسجيل حدث أمني
     */
    @Transactional
    public void logSecurityEvent(UUID actorId, String action, String details) {
        logEvent(actorId, "SECURITY_" + action, "security", "system",
                Map.of("details", details, "severity", "HIGH"));
    }

    /**
     * تسجيل حدث مصادقة
     */
    @Transactional
    public void logAuthEvent(UUID actorId, String action, String status, String ipAddress) {
        logEvent(actorId, "AUTH_" + action, "auth", actorId != null ? actorId.toString() : "unknown",
                Map.of("status", status, "ip_address", ipAddress));
    }

    /**
     * تسجيل حدث محادثة
     */
    @Transactional
    public void logConversationEvent(UUID actorId, String action, String conversationId) {
        logEvent(actorId, "CONVERSATION_" + action, "conversation", conversationId,
                Map.of("conversation_id", conversationId));
    }

    /**
     * تسجيل حدث رسالة
     */
    @Transactional
    public void logMessageEvent(UUID actorId, String action, String messageId, String conversationId) {
        logEvent(actorId, "MESSAGE_" + action, "message", messageId,
                Map.of("message_id", messageId, "conversation_id", conversationId));
    }

    /**
     * تسجيل حدث مكالمة
     */
    @Transactional
    public void logCallEvent(UUID actorId, String action, String callId, String callType) {
        logEvent(actorId, "CALL_" + action, "call", callId,
                Map.of("call_id", callId, "call_type", callType));
    }

    /**
     * تسجيل حدث إشراف
     */
    @Transactional
    public void logModerationEvent(UUID actorId, String action, String targetType, String targetId, String reason) {
        logEvent(actorId, "MODERATION_" + action, targetType, targetId,
                Map.of("reason", reason, "moderator_id", actorId.toString()));
    }

    /**
     * تسجيل حدث نظام
     */
    @Transactional
    public void logSystemEvent(String action, String component, Map<String, Object> details) {
        logEvent(null, "SYSTEM_" + action, "system", component, details);
    }

    /**
     * البحث في سجلات التدقيق
     */
//    @Transactional(readOnly = true)
//    public List<AuditLog> searchAuditLogs(UUID actorId, String action, String targetType,
//                                          OffsetDateTime startDate, OffsetDateTime endDate) {
//        return auditLogRepository.findByCriteria(actorId, action, targetType, startDate, endDate);
//    }

    /**
     * الحصول على سجلات مستخدم معين
     */
//    @Transactional(readOnly = true)
//    public List<AuditLog> getUserAuditLogs(UUID userId, int limit) {
//        return auditLogRepository.findByActorUserIdOrderByCreatedAtDesc(userId, limit);
//    }

    /**
     * التحقق من الأحداث المشبوهة
     */
    @Transactional(readOnly = true)
    public boolean hasSuspiciousActivity(UUID userId, int hours) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(hours);
        long securityEvents = auditLogRepository.countSecurityEvents(userId, since);

        return securityEvents > 10; // أكثر من 10 أحداث أمنية في الفترة المحددة
    }

    /**
     * تحويل الخريطة إلى JSON
     */
    private String convertMapToJson(Map<String, Object> map) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"error\": \"Failed to serialize metadata\"}";
        }
    }

    /**
     * تحليل JSON إلى خريطة
     */
    private Map<String, Object> parseJsonToMap(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Map.of("error", "Failed to parse metadata");
        }
    }
}
