//package com.nova.poneglyph.config.websocket;
//
//import com.nova.poneglyph.service.SessionService;
//import com.nova.poneglyph.service.presence.PresenceService;
//import com.nova.poneglyph.service.WebSocketService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.dao.OptimisticLockingFailureException;
//import org.springframework.messaging.MessageDeliveryException;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Duration;
//import java.time.OffsetDateTime;
//
//@Service
//public class WebSocketMaintenanceService {
//
//    private static final Logger log = LoggerFactory.getLogger(WebSocketMaintenanceService.class);
//    private static final Duration ONLINE_TIMEOUT = Duration.ofSeconds(5);
//
//    private final SessionService sessionService;
//    private final PresenceService presenceService;
//    private final WebSocketService webSocketService;
//
//    public WebSocketMaintenanceService(SessionService sessionService,
//                                       PresenceService presenceService,
//                                       WebSocketService webSocketService) {
//        this.sessionService = sessionService;
//        this.presenceService = presenceService;
//        this.webSocketService = webSocketService;
//    }
//
//    /**
//     * تنظيف الجلسات الغير نشطة كل 30 ثانية.
//     * إذا كانت الجلسة منتهية في DB ولم تعد موجودة في Redis -> نضعها offline فوريًا.
//     * إذا كانت موجودة في Redis لكن DB منتهي -> نجدد lastActivity في DB
//     *
//     * ملاحظات الأمان:
//     * - عند محاولة مسح websocketSessionId نستخدم طريقة آمنة مع retry/pessimistic lock
//     *   لحماية من OptimisticLockingFailureException.
//     */
//    @Scheduled(fixedRate = 30000)
//    @Transactional
//    public void cleanupInactiveSessions() {
//        try {
//            OffsetDateTime threshold = OffsetDateTime.now().minus(ONLINE_TIMEOUT);
//
//            sessionService.getAllActiveSessions().forEach(session -> {
//                try {
//                    boolean isActiveInRedis = presenceService.isUserOnline(session.getUser().getId());
//                    OffsetDateTime lastActivity = session.getLastActivity();
//                    boolean isExpired = lastActivity == null || lastActivity.isBefore(threshold);
//
//                    if (!isActiveInRedis && isExpired) {
//                        log.info("Cleaning up inactive session: {} for user: {} (lastActivity={})",
//                                session.getId(), session.getUser().getId(), lastActivity);
//
//                        // محاولة استخدام قفل متشائم (pessimistic) أولاً إذا كانت متاحة في SessionService.
//                        try {
//                            sessionService.clearWebsocketSessionIdWithPessimisticLock(session.getId());
//                            log.debug("Cleared websocketSessionId (pessimistic) for session {}", session.getId());
//                        } catch (UnsupportedOperationException e) {
//                            // لو لم تقم بتطبيق findByIdForUpdate / method غير موجود، استخدم retry
//                            log.debug("Pessimistic lock method unavailable, using retry for session {}", session.getId());
//                            sessionService.clearWebsocketSessionIdWithRetry(session.getId());
//                        } catch (OptimisticLockingFailureException lockEx) {
//                            // لو حصل تعارض رغم القفل — جرب retry ثم fallback
//                            log.warn("Lock conflict clearing session {}: {}, attempting retry", session.getId(), lockEx.getMessage());
//                            try {
//                                sessionService.clearWebsocketSessionIdWithRetry(session.getId());
//                            } catch (Exception exRetry) {
//                                log.error("Retry also failed for clearing session {}: {}, falling back to presence update", session.getId(), exRetry.getMessage());
//                                // fallback: رغم فشل DB، نضمن تحديث Redis و notify فورياً
//                                presenceService.updateOnlineStatusImmediate(session.getUser().getId(), session.getId(), false, null);
//                            }
//                        } catch (Exception e) {
//                            // أي استثناء آخر — لا نفشل العملية الكاملة، نعمل fallback
//                            log.error("Failed clearing websocketSessionId for {}: {}, falling back to presence update", session.getId(), e.getMessage());
//                            presenceService.updateOnlineStatusImmediate(session.getUser().getId(), session.getId(), false, null);
//                        }
//
//                        // تأكّد من تحديث Redis وارسال notify (في حال لم يُسبّبها السلوك أعلاه)
//                        presenceService.updateOnlineStatusImmediate(session.getUser().getId(), session.getId(), false, null);
//
//                    } else if (isActiveInRedis && isExpired) {
//                        // وسطية -> نجدد lastActivity في DB لأن Redis تقول أنه لا يزال نشط
//                        session.setLastActivity(OffsetDateTime.now());
//                        try {
//                            sessionService.saveSession(session);
//                            log.debug("Refreshed expired but active session: {} (was lastActivity={})", session.getId(), lastActivity);
//                        } catch (OptimisticLockingFailureException lockEx) {
//                            log.warn("Optimistic lock when refreshing expired but active session {}: {}", session.getId(), lockEx.getMessage());
//                            // في حالة النزاع: لا نكسر الجدولة، يمكن تجاهل أو إعادة جدولة التحديث لاحقاً
//                        } catch (Exception e) {
//                            log.error("Failed to save refreshed session {}: {}", session.getId(), e.getMessage(), e);
//                        }
//                    }
//                } catch (Exception perSessionEx) {
//                    // التأكد أن استثناء في جلسة واحدة لا يوقف معالجة الباقي
//                    log.error("Failed processing session {} during cleanup: {}", session.getId(), perSessionEx.getMessage(), perSessionEx);
//                }
//            });
//        } catch (Exception e) {
//            log.error("Failed to cleanup inactive sessions: {}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * إرسال heartbeat مجدول لكن تنفيذ الإرسال فوري.
//     * هذا يعمل كشبكة أمان لتجديد صلاحية المفاتيح في Redis.
//     *
//     * في حال فشل تسليم الرسالة نستخدم نفس آلية التعامل مع disconnect (clear+fallback).
//     */
//    @Scheduled(fixedRate = 10000)
//    public void sendHeartbeats() {
//        try {
//            sessionService.getAllActiveSessions().forEach(session -> {
//                if (session.isOnline() && session.getWebsocketSessionId() != null) {
//                    try {
//                        webSocketService.sendHeartbeat(session.getUser().getId().toString());
//                        presenceService.handleHeartbeatImmediate(session.getUser().getId(), session.getId());
//                    } catch (Exception e) {
//                        log.warn("Failed to send heartbeat to session {}: {}", session.getId(), e.getMessage());
//                        if (e instanceof MessageDeliveryException) {
//                            // محاولة تنظيف الجلسة بأمان مع retry/pessimistic
//                            try {
//                                sessionService.clearWebsocketSessionIdWithPessimisticLock(session.getId());
//                            } catch (UnsupportedOperationException ue) {
//                                sessionService.clearWebsocketSessionIdWithRetry(session.getId());
//                            } catch ( OptimisticLockingFailureException lockEx) {
//                                log.warn("Lock conflict clearing session {} after heartbeat failure: {}", session.getId(), lockEx.getMessage());
//                                try {
//                                    sessionService.clearWebsocketSessionIdWithRetry(session.getId());
//                                } catch (Exception retryEx) {
//                                    log.error("Retry failed for clearing session {}: {}, using presence fallback", session.getId(), retryEx.getMessage());
//                                }
//                            } catch (Exception ex) {
//                                log.error("Unexpected error clearing websocketSessionId for {}: {}", session.getId(), ex.getMessage(), ex);
//                            } finally {
//                                // تأكد من تحديث Redis/notify حتى لو فشلت DB
//                                try {
//                                    presenceService.updateOnlineStatusImmediate(session.getUser().getId(), session.getId(), false, null);
//                                } catch (Exception presenceEx) {
//                                    log.warn("Failed to update presence fallback for session {}: {}", session.getId(), presenceEx.getMessage());
//                                }
//                            }
//                        }
//                    }
//                }
//            });
//        } catch (Exception e) {
//            log.error("Failed to send heartbeats: {}", e.getMessage(), e);
//        }
//    }
//}
package com.nova.poneglyph.config.websocket;

import com.nova.poneglyph.domain.user.UserSession;
import com.nova.poneglyph.service.SessionService;
import com.nova.poneglyph.service.presence.PresenceService;
import com.nova.poneglyph.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
public class WebSocketMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMaintenanceService.class);
    private static final Duration ONLINE_TIMEOUT = Duration.ofSeconds(5);

    private final SessionService sessionService;
    private final PresenceService presenceService;
    private final WebSocketService webSocketService;

    public WebSocketMaintenanceService(SessionService sessionService,
                                       PresenceService presenceService,
                                       WebSocketService webSocketService) {
        this.sessionService = sessionService;
        this.presenceService = presenceService;
        this.webSocketService = webSocketService;
    }

    /**
     * تنظيف الجلسات الغير نشطة كل 30 ثانية.
     * كل عملية تحديث DB تتم عبر طرق آمنة داخل SessionService (مع retry أو pessimistic).
     * لا نعلّم هذه الدالة @Transactional حتى لا نجمع تعديلات كثيرة ضمن معاملة واحدة.
     */
    @Scheduled(fixedRate = 30000)
    public void cleanupInactiveSessions() {
        try {
            OffsetDateTime threshold = OffsetDateTime.now().minus(ONLINE_TIMEOUT);

            sessionService.getAllActiveSessions().forEach(session -> {
                try {
                    boolean isActiveInRedis = presenceService.isUserOnline(session.getUser().getId());
                    OffsetDateTime lastActivity = session.getLastActivity();
                    boolean isExpired = lastActivity == null || lastActivity.isBefore(threshold);

                    if (!isActiveInRedis && isExpired) {
                        log.info("Cleaning up inactive session: {} for user: {} (lastActivity={})",
                                session.getId(), session.getUser().getId(), lastActivity);

                        // حاول قفل متشائم أولاً، ثم retry، ثم fallback على Redis/notify
                        try {
                            sessionService.clearWebsocketSessionIdWithPessimisticLock(session.getId());
                        } catch (Exception e) {
                            log.debug("Pessimistic lock failed/unsupported for {}, trying retry", session.getId());
                            sessionService.clearWebsocketSessionIdWithRetry(session.getId());
                        }

                        // تأكد من تحديث Redis وارسال notify فورياً
                        try {
                            presenceService.updateOnlineStatusImmediate(session.getUser().getId(), session.getId(), false, null);
                        } catch (Exception e) {
                            log.warn("Failed presence fallback for session {}: {}", session.getId(), e.getMessage());
                        }

                    } else if (isActiveInRedis && isExpired) {
                        boolean ok = sessionService.updateLastActivitySafe(session.getId());
                        if (ok) {
                            log.debug("Refreshed expired but active session: {} (was lastActivity={})", session.getId(), lastActivity);
                        } else {
                            log.warn("Failed to safely refresh lastActivity for session {}", session.getId());
                        }
                    }
                } catch (Exception perSessionEx) {
                    log.error("Failed processing session {} during cleanup: {}", session.getId(), perSessionEx.getMessage(), perSessionEx);
                }
            });
        } catch (Exception e) {
            log.error("Failed to cleanup inactive sessions: {}", e.getMessage(), e);
        }
    }

    /**
     * إرسال heartbeats كل 10 ثواني: التجديد في Redis فوري عبر PresenceService.handleHeartbeatImmediate.
     */
    @Scheduled(fixedRate = 10000)
    public void sendHeartbeats() {
        try {
            sessionService.getAllActiveSessions().forEach((UserSession session) -> {
                if (session.isOnline() && session.getWebsocketSessionId() != null) {
                    try {
                        webSocketService.sendHeartbeat(session.getUser().getId().toString());
                        presenceService.handleHeartbeatImmediate(session.getUser().getId(), session.getId());
                    } catch (Exception e) {
                        log.warn("Failed to send heartbeat to session {}: {}", session.getId(), e.getMessage());
                        if (e instanceof MessageDeliveryException) {
                            try {
                                sessionService.clearWebsocketSessionIdWithPessimisticLock(session.getId());
                            } catch (Exception ex) {
                                sessionService.clearWebsocketSessionIdWithRetry(session.getId());
                            } finally {
                                try {
                                    presenceService.updateOnlineStatusImmediate(session.getUser().getId(), session.getId(), false, null);
                                } catch (Exception presenceEx) {
                                    log.warn("Failed presence fallback after heartbeat failure for {}: {}", session.getId(), presenceEx.getMessage());
                                }
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.error("Failed to send heartbeats: {}", e.getMessage(), e);
        }
    }
}
