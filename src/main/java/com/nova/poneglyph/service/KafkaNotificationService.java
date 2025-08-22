//package com.nova.poneglyph.service;
//
//
//import com.nova.poneglyph.events.NotificationEvent;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.common.PartitionInfo;
//import org.apache.kafka.common.TopicPartition;
//import org.springframework.kafka.annotation.KafkaListener;
//
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//
//
///*------------       v2           ------------*/
////@Service
////@Slf4j
////@RequiredArgsConstructor
////public class KafkaNotificationService {
////    private final SimpMessagingTemplate messagingTemplate;
////    private final PresenceService presenceService;
////    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
////    private final KafkaConsumer<String, NotificationEvent> pendingNotificationsConsumer;
////    private final Set<String> processedNotifications = ConcurrentHashMap.newKeySet();
////
////    private static final String NOTIFICATIONS_TOPIC = "notifications-topic";
////    private static final String PENDING_TOPIC = "pending-notifications";
////    private static final String CONNECTION_EVENTS_TOPIC = "connection-events";
////    private static final long RESEND_TIMEOUT_MS = 30000; // 30 ثانية
////
////    @KafkaListener(topics = NOTIFICATIONS_TOPIC, groupId = "realtime-group")
////    public void handleRealTimeNotification(NotificationEvent event) {
////        if (presenceService.isUserConnected(event.getUserId())) {
////            sendViaWebSocket(event);
////        } else {
////            storeForLaterDelivery(event);
////        }
////    }
////
////    @KafkaListener(topics = PENDING_TOPIC, groupId = "pending-group")
////    public void handlePendingNotification(ConsumerRecord<String, NotificationEvent> record) {
////        NotificationEvent event = record.value();
////        if (presenceService.isUserConnected(event.getUserId())) {
////            sendViaWebSocket(event);
////        }
////    }
////
////    @KafkaListener(topics = CONNECTION_EVENTS_TOPIC, groupId = "connection-group")
////    public void handleConnectionEvent(ConsumerRecord<String, NotificationEvent> record) {
////        String userId = record.key();
////        log.info("Processing connection event for user: {}", userId);
////
////        // تنظيف الإشعارات المعالجة القديمة
////        if (processedNotifications.size() > 1000) {
////            processedNotifications.clear();
////        }
////
////        resendPendingNotifications(userId);
////
////    }
////
////
////
////    private void storeForLaterDelivery(NotificationEvent event) {
////        kafkaTemplate.send(PENDING_TOPIC, event.getUserId(), event);
////        log.info("Stored notification for user {} in pending topic", event.getUserId());
////    }
////
////
////    private void resendPendingNotifications(String userId) {
////        try {
////            List<PartitionInfo> partitions = pendingNotificationsConsumer.partitionsFor(PENDING_TOPIC);
////            List<TopicPartition> topicPartitions = partitions.stream()
////                    .map(p -> new TopicPartition(PENDING_TOPIC, p.partition()))
////                    .collect(Collectors.toList());
////
////            pendingNotificationsConsumer.assign(topicPartitions);
////            pendingNotificationsConsumer.seekToBeginning(topicPartitions);
////
////            Instant startTime = Instant.now();
////            int resentCount = 0;
////
////            while (Duration.between(startTime, Instant.now()).toMillis() < RESEND_TIMEOUT_MS) {
////                ConsumerRecords<String, NotificationEvent> records =
////                        pendingNotificationsConsumer.poll(Duration.ofMillis(500));
////
////                if (records.isEmpty()) {
////                    break;
////                }
////
////                for (ConsumerRecord<String, NotificationEvent> record : records) {
////                    if (record.key().equals(userId) &&
////                            !processedNotifications.contains(record.value().getNotificationId())) {
////
////                        sendViaWebSocket(record.value());
////                        processedNotifications.add(record.value().getNotificationId());
////                        resentCount++;
////                    }
////                }
////            }
////
////            log.info("Resent {} pending notifications for user: {}", resentCount, userId);
////        } catch (Exception e) {
////            log.error("Error resending pending notifications for user: {}", userId, e);
////        } finally {
////            pendingNotificationsConsumer.unsubscribe();
////        }
////    }
////
////    private String getDestination(NotificationEvent.EventType type) {
////        return switch (type) {
////            case MESSAGE, DELIVERY, SEEN -> "/queue/messages";
////            case CONVERSATION_UPDATE -> "/queue/conversation-updates";
////            case PRESENCE -> "/queue/presence";
////            case SYSTEM, ALERT -> "/queue/alerts";
////            case TYPING -> "/queue/typing";
////        };
////    }
////
////    private Object getPayload(NotificationEvent event) {
////        return switch (event.getEventType()) {
////            case MESSAGE, DELIVERY, SEEN -> event.getMessage();
////            case CONVERSATION_UPDATE -> event.getConversationUpdate();
////            case PRESENCE, TYPING -> event.getPresence();
////            case SYSTEM, ALERT -> event.getSystemMessage() + " [Kafka]";
////        };
////    }
////
////    private final Map<String, Long> lastNotificationTime = new ConcurrentHashMap<>();
////
////    private void sendViaWebSocket(NotificationEvent event) {
////        // تجنب إرسال نفس الإشعار أكثر من مرة في 5 دقائق
////        long now = System.currentTimeMillis();
////        long lastSent = lastNotificationTime.getOrDefault(event.getNotificationId(), 0L);
////
////        if (now - lastSent > 300000) { // 5 دقائق
////            String destination = getDestination(event.getEventType());
////            Object payload = getPayload(event);
////
////            messagingTemplate.convertAndSendToUser(
////                    event.getUserId(),
////                    destination,
////                    payload
////            );
////            lastNotificationTime.put(event.getNotificationId(), now);
////            log.info("Sent notification to user {} via WebSocket", event.getUserId());
////        }
////    }
//////}
////import org.springframework.data.redis.core.StringRedisTemplate;
////import java.util.concurrent.TimeUnit;
////
////@Service
////@Slf4j
////@RequiredArgsConstructor
////public class KafkaNotificationService {
////    private final SimpMessagingTemplate messagingTemplate;
////    private final PresenceService presenceService;
////    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
////    private final KafkaConsumer<String, NotificationEvent> pendingNotificationsConsumer;
////    private final StringRedisTemplate redisTemplate; // <-- جديد
////
////    private static final String NOTIFICATIONS_TOPIC = "notifications-topic";
////    private static final String PENDING_TOPIC = "pending-notifications";
////    private static final String CONNECTION_EVENTS_TOPIC = "connection-events";
////    private static final long RESEND_TIMEOUT_MS = 30000; // 30 ثانية
////    private static final long PROCESSED_TTL_DAYS = 1;    // يوم واحد
////
////    @KafkaListener(topics = NOTIFICATIONS_TOPIC, groupId = "realtime-group")
////    public void handleRealTimeNotification(NotificationEvent event) {
////        if (isAlreadyProcessed(event.getNotificationId())) {
////            log.info("Notification {} already processed, skipping", event.getNotificationId());
////            return;
////        }
////        if (presenceService.isUserConnected(event.getUserId())) {
////            sendViaWebSocket(event);
////        } else {
////            storeForLaterDelivery(event);
////        }
////    }
////
////    @KafkaListener(topics = PENDING_TOPIC, groupId = "pending-group")
////    public void handlePendingNotification(ConsumerRecord<String, NotificationEvent> record) {
////        NotificationEvent event = record.value();
////        if (!isAlreadyProcessed(event.getNotificationId()) &&
////                presenceService.isUserConnected(event.getUserId())) {
////            sendViaWebSocket(event);
////        }
////    }
////
////    @KafkaListener(topics = CONNECTION_EVENTS_TOPIC, groupId = "connection-group")
////    public void handleConnectionEvent(ConsumerRecord<String, NotificationEvent> record) {
////        String userId = record.key();
////        log.info("Processing connection event for user: {}", userId);
////        resendPendingNotifications(userId);
////    }
////
////    private void storeForLaterDelivery(NotificationEvent event) {
////        kafkaTemplate.send(PENDING_TOPIC, event.getUserId(), event);
////        log.info("Stored notification for user {} in pending topic", event.getUserId());
////    }
////
////    private void resendPendingNotifications(String userId) {
////        try {
////            List<PartitionInfo> partitions = pendingNotificationsConsumer.partitionsFor(PENDING_TOPIC);
////            List<TopicPartition> topicPartitions = partitions.stream()
////                    .map(p -> new TopicPartition(PENDING_TOPIC, p.partition()))
////                    .toList();
////
////            pendingNotificationsConsumer.assign(topicPartitions);
////            pendingNotificationsConsumer.seekToBeginning(topicPartitions);
////
////            Instant startTime = Instant.now();
////            int resentCount = 0;
////
////            while (Duration.between(startTime, Instant.now()).toMillis() < RESEND_TIMEOUT_MS) {
////                ConsumerRecords<String, NotificationEvent> records =
////                        pendingNotificationsConsumer.poll(Duration.ofMillis(500));
////
////                if (records.isEmpty()) {
////                    break;
////                }
////
////                for (ConsumerRecord<String, NotificationEvent> record : records) {
////                    NotificationEvent event = record.value();
////                    if (record.key().equals(userId) && !isAlreadyProcessed(event.getNotificationId())) {
////                        sendViaWebSocket(event);
////                        resentCount++;
////                    }
////                }
////            }
////
////            log.info("Resent {} pending notifications for user: {}", resentCount, userId);
////        } catch (Exception e) {
////            log.error("Error resending pending notifications for user: {}", userId, e);
////        } finally {
////            pendingNotificationsConsumer.unsubscribe();
////        }
////    }
////
////    private boolean isAlreadyProcessed(String notificationId) {
////        Boolean exists = redisTemplate.hasKey("processed:" + notificationId);
////        return exists != null && exists;
////    }
////
////    private void markAsProcessed(String notificationId) {
////        redisTemplate.opsForValue().set(
////                "processed:" + notificationId, "1", PROCESSED_TTL_DAYS, TimeUnit.DAYS
////        );
////    }
////
////    private String getDestination(NotificationEvent.EventType type) {
////        return switch (type) {
////            case MESSAGE, DELIVERY, SEEN -> "/queue/messages";
////            case CONVERSATION_UPDATE -> "/queue/conversation-updates";
////            case PRESENCE -> "/queue/presence";
////            case SYSTEM, ALERT -> "/queue/alerts";
////            case TYPING -> "/queue/typing";
////        };
////    }
////
////    private Object getPayload(NotificationEvent event) {
////        return switch (event.getEventType()) {
////            case MESSAGE, DELIVERY, SEEN -> event.getMessage();
////            case CONVERSATION_UPDATE -> event.getConversationUpdate();
////            case PRESENCE, TYPING -> event.getPresence();
////            case SYSTEM, ALERT -> event.getSystemMessage() + " [Kafka]";
////        };
////    }
////
////    private final Map<String, Long> lastNotificationTime = new ConcurrentHashMap<>();
////
////    private void sendViaWebSocket(NotificationEvent event) {
////        long now = System.currentTimeMillis();
////        long lastSent = lastNotificationTime.getOrDefault(event.getNotificationId(), 0L);
////
////        if (now - lastSent > 300000) { // 5 دقائق
////            String destination = getDestination(event.getEventType());
////            Object payload = getPayload(event);
////
////            messagingTemplate.convertAndSendToUser(
////                    event.getUserId(),
////                    destination,
////                    payload
////            );
////
////            lastNotificationTime.put(event.getNotificationId(), now);
////            markAsProcessed(event.getNotificationId()); // <-- تسجيل في Redis
////            log.info("Sent notification to user {} via WebSocket", event.getUserId());
////        }
////    }
////}
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class KafkaNotificationService {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final PresenceService presenceService;
//    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
//    private final KafkaConsumer<String, NotificationEvent> pendingNotificationsConsumer;
//    private final StringRedisTemplate redisTemplate;
//    private final Map<NotificationEvent.EventType, Duration> notificationTTLs; // يأتي من KafkaRedisConfig
//
//    private static final String NOTIFICATIONS_TOPIC = "notifications-topic";
//    private static final String PENDING_TOPIC = "pending-notifications";
//    private static final String CONNECTION_EVENTS_TOPIC = "connection-events";
//    private static final long RESEND_TIMEOUT_MS = 30000; // 30 ثانية
//
//    private final Map<String, Long> lastNotificationTime = new ConcurrentHashMap<>();
//
//    @KafkaListener(topics = NOTIFICATIONS_TOPIC, groupId = "realtime-group")
//    public void handleRealTimeNotification(NotificationEvent event) {
//        if (isAlreadyProcessed(event.getNotificationId())) return;
//        if (presenceService.isUserConnected(event.getUserId())) sendViaWebSocket(event);
//        else storeForLaterDelivery(event);
//    }
//
//    @KafkaListener(topics = PENDING_TOPIC, groupId = "pending-group")
//    public void handlePendingNotification(ConsumerRecord<String, NotificationEvent> record) {
//        NotificationEvent event = record.value();
//        if (!isAlreadyProcessed(event.getNotificationId()) && presenceService.isUserConnected(event.getUserId())) {
//            sendViaWebSocket(event);
//        }
//    }
//
//    @KafkaListener(topics = CONNECTION_EVENTS_TOPIC, groupId = "connection-group")
//    public void handleConnectionEvent(ConsumerRecord<String, NotificationEvent> record) {
//        String userId = record.key();
//        log.info("Processing connection event for user: {}", userId);
//        resendPendingNotifications(userId);
//    }
//
//    private void storeForLaterDelivery(NotificationEvent event) {
//        kafkaTemplate.send(PENDING_TOPIC, event.getUserId(), event);
//        log.info("Stored notification for user {} in pending topic", event.getUserId());
//    }
//
//    private void resendPendingNotifications(String userId) {
//        try {
//            List<PartitionInfo> partitions = pendingNotificationsConsumer.partitionsFor(PENDING_TOPIC);
//            List<TopicPartition> topicPartitions = partitions.stream()
//                    .map(p -> new TopicPartition(PENDING_TOPIC, p.partition()))
//                    .toList();
//
//            pendingNotificationsConsumer.assign(topicPartitions);
//            pendingNotificationsConsumer.seekToBeginning(topicPartitions);
//
//            Instant startTime = Instant.now();
//            int resentCount = 0;
//
//            while (Duration.between(startTime, Instant.now()).toMillis() < RESEND_TIMEOUT_MS) {
//                ConsumerRecords<String, NotificationEvent> records = pendingNotificationsConsumer.poll(Duration.ofMillis(500));
//                if (records.isEmpty()) break;
//
//                for (ConsumerRecord<String, NotificationEvent> record : records) {
//                    NotificationEvent event = record.value();
//                    if (record.key().equals(userId) && !isAlreadyProcessed(event.getNotificationId())) {
//                        sendViaWebSocket(event);
//                        resentCount++;
//                    }
//                }
//            }
//
//            log.info("Resent {} pending notifications for user: {}", resentCount, userId);
//        } catch (Exception e) {
//            log.error("Error resending pending notifications for user: {}", userId, e);
//        } finally {
//            pendingNotificationsConsumer.unsubscribe();
//        }
//    }
//
//    private boolean isAlreadyProcessed(String notificationId) {
//        Boolean exists = redisTemplate.hasKey("processed:" + notificationId);
//        return exists != null && exists;
//    }
//
//    /**
//     * تسجيل الإشعار كمُعالج في Redis مع TTL ديناميكي حسب النوع
//     */
//    private void markAsProcessed(NotificationEvent event) {
//        Duration ttl = notificationTTLs.getOrDefault(event.getEventType(), Duration.ofHours(1));
//        redisTemplate.opsForValue().set("processed:" + event.getNotificationId(), "1", ttl);
//    }
//
//    private String getDestination(NotificationEvent.EventType type) {
//        return switch (type) {
//            case MESSAGE, DELIVERY, SEEN -> "/queue/messages";
//            case CONVERSATION_UPDATE -> "/queue/conversation-updates";
//            case PRESENCE -> "/queue/presence";
//            case SYSTEM, ALERT -> "/queue/alerts";
//            case TYPING -> "/queue/typing";
//        };
//    }
//
//    private Object getPayload(NotificationEvent event) {
//        return switch (event.getEventType()) {
//            case MESSAGE, DELIVERY, SEEN -> event.getMessage();
//            case CONVERSATION_UPDATE -> event.getConversationUpdate();
//            case PRESENCE, TYPING -> event.getPresence();
//            case SYSTEM, ALERT -> event.getSystemMessage() + " [Kafka]";
//        };
//    }
//
//    private void sendViaWebSocket(NotificationEvent event) {
//        long now = System.currentTimeMillis();
//        long lastSent = lastNotificationTime.getOrDefault(event.getNotificationId(), 0L);
//
//        if (now - lastSent > 300000) { // 5 دقائق
//            String destination = getDestination(event.getEventType());
//            Object payload = getPayload(event);
//
//            messagingTemplate.convertAndSendToUser(event.getUserId(), destination, payload);
//
//            lastNotificationTime.put(event.getNotificationId(), now);
//            markAsProcessed(event); // TTL ديناميكي حسب نوع الإشعار
//            log.info("Sent notification to user {} via WebSocket", event.getUserId());
//
//        }
//    }
//}
//
////@Service
////@Slf4j
////@RequiredArgsConstructor
////public class KafkaNotificationService {
////
////    private final SimpMessagingTemplate messagingTemplate;
////    private final PresenceService presenceService;
////    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
////    private final RedisTemplate<String, NotificationEvent> redisTemplate;
////
////    private static final String NOTIFICATIONS_TOPIC = "notifications-topic";
////    private static final String PENDING_KEY_PREFIX = "pending:";
////    private static final String SENT_KEY_PREFIX = "sent:";
////
////    @KafkaListener(topics = NOTIFICATIONS_TOPIC, groupId = "realtime-group")
////    public void handleRealTimeNotification(NotificationEvent event) {
////        if (presenceService.isUserConnected(event.getUserId())) {
////            sendViaWebSocket(event);
////        } else {
////            storePendingInRedis(event);
////        }
////    }
////
////    @KafkaListener(topics = "connection-events", groupId = "connection-group")
////    public void handleConnectionEvent(ConsumerRecord<String, NotificationEvent> record) {
////        String userId = record.key();
////        log.info("User {} reconnected, resending pending notifications", userId);
////        resendPendingFromRedis(userId);
////    }
////
////    private void storePendingInRedis(NotificationEvent event) {
////        String redisKey = PENDING_KEY_PREFIX + event.getUserId();
////        redisTemplate.opsForList().rightPush(redisKey, event);
////        // نضع TTL حتى لا تتكدس الرسائل القديمة
////        redisTemplate.expire(redisKey, Duration.ofHours(24));
////        log.info("Stored notification {} for user {} in Redis", event.getNotificationId(), event.getUserId());
////    }
////
////    private void resendPendingFromRedis(String userId) {
////        String redisKey = PENDING_KEY_PREFIX + userId;
////        Long size = redisTemplate.opsForList().size(redisKey);
////        if (size == null || size == 0) return;
////
////        for (int i = 0; i < size; i++) {
////            NotificationEvent event = redisTemplate.opsForList().leftPop(redisKey);
////            if (event != null && !isDuplicate(event)) {
////                sendViaWebSocket(event);
////                markAsSent(event);
////            }
////        }
////        log.info("Resent all pending notifications for user {}", userId);
////    }
////
////    private boolean isDuplicate(NotificationEvent event) {
////        String key = SENT_KEY_PREFIX + event.getNotificationId();
////        Boolean exists = redisTemplate.hasKey(key);
////        return exists != null && exists;
////    }
////
////    private void markAsSent(NotificationEvent event) {
////        String key = SENT_KEY_PREFIX + event.getNotificationId();
////        redisTemplate.opsForValue().set(key, event);
////        redisTemplate.expire(key, Duration.ofMinutes(5)); // deduplication window
////    }
////
////    private void sendViaWebSocket(NotificationEvent event) {
////        if (!isDuplicate(event)) {
////            String destination = getDestination(event.getEventType());
////            Object payload = getPayload(event);
////            messagingTemplate.convertAndSendToUser(event.getUserId(), destination, payload);
////            markAsSent(event);
////            log.info("Sent notification {} to user {} via WebSocket", event.getNotificationId(), event.getUserId());
////        }
////    }
////
////    private String getDestination(NotificationEvent.EventType type) {
////        return switch (type) {
////            case MESSAGE, DELIVERY, SEEN -> "/queue/messages";
////            case CONVERSATION_UPDATE -> "/queue/conversation-updates";
////            case PRESENCE -> "/queue/presence";
////            case SYSTEM, ALERT -> "/queue/alerts";
////            case TYPING -> "/queue/typing";
////        };
////    }
////
////    private Object getPayload(NotificationEvent event) {
////        return switch (event.getEventType()) {
////            case MESSAGE, DELIVERY, SEEN -> event.getMessage();
////            case CONVERSATION_UPDATE -> event.getConversationUpdate();
////            case PRESENCE, TYPING -> event.getPresence();
////            case SYSTEM, ALERT -> event.getSystemMessage();
////        };
////    }
////}
//
