//
//package com.nova.poneglyph.service;
//
//import com.nova.poneglyph.config.PresenceNotifier;
//import com.nova.poneglyph.dto.PresenceDTO;
//import com.nova.poneglyph.dto.TypingEventRequest;
//import com.nova.poneglyph.events.NotificationEvent;
//import com.nova.poneglyph.events.TypingEvent;
//import com.nova.poneglyph.mapper.UserMapper;
//import com.nova.poneglyph.model.Conversation;
//import com.nova.poneglyph.model.User;
//import com.nova.poneglyph.repository.ConversationRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.dto.UserDto;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.ApplicationContext;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class PresenceService implements PresenceNotifier {
//
//    private final UserRepository userRepository;
//    private final ConversationRepository conversationRepository;
//    private final ApplicationContext applicationContext;
//    private final KafkaTemplate<String,NotificationEvent> kafkaTemplate;
//
//    private final Map<String, Long> lastHeartbeatMap = new ConcurrentHashMap<>();
//    private static final long HEARTBEAT_TIMEOUT = 60000; // 60 ثانية
//
//    private static final String CONNECTION_EVENTS_TOPIC = "connection-events";
//
//
//    private SimpMessagingTemplate getMessagingTemplate() {
//        return applicationContext.getBean(SimpMessagingTemplate.class);
//    }
//
//    /** حدث الكتابة */
//    public void handleTypingEvent(TypingEventRequest request) {
//        TypingEvent typingEvent = new TypingEvent(
//                request.getConversationId(),
//                request.getSenderPhone(),
//                request.getReceiverPhone(),
//                request.isTyping()
//        );
//        getMessagingTemplate().convertAndSendToUser(
//                request.getReceiverPhone(),
//                "/queue/typing",
//                typingEvent
//        );
//    }
//
//    /** الحصول على حالة المستخدم */
//    public PresenceDTO getUserPresence(String phoneNumber) {
//        User user = userRepository.findByPhoneNumber(phoneNumber)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        return PresenceDTO.builder()
//                .phoneNumber(phoneNumber)
//                .online(user.isOnline())
//                .lastSeen(user.getLastSeen())
//                .status(user.isOnline() ? "online" : "offline")
//                .build();
//    }
//
//    /** تحديث حالة المستخدم وإشعار جهات الاتصال */
//    public void updateUserPresence(String phoneNumber, boolean isOnline) {
//        User user = userRepository.findByPhoneNumber(phoneNumber)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        user.setOnline(isOnline);
//        user.setLastSeen(isOnline ? null : LocalDateTime.now());
//        userRepository.save(user);
//
//        notifyPresenceToContacts(phoneNumber, isOnline);
//    }
//
//    /** إشعار جهات الاتصال بتغير حالة المستخدم */
//    private void notifyPresenceToContacts(String phoneNumber, boolean isOnline) {
//        List<Conversation> conversations = conversationRepository
//                .findByParticipantPhonesContaining(phoneNumber);
//
//        UserDto userDto = UserMapper.toDTO(
//                userRepository.findByPhoneNumber(phoneNumber).orElseThrow(),
//                true
//        );
//
//        conversations.forEach(conversation -> conversation.getParticipantIds().stream()
//                .filter(participant -> !participant.equals(phoneNumber))
//                .forEach(participant -> {
//                    getMessagingTemplate().convertAndSendToUser(
//                            participant,
//                            "/queue/presence",
//                            userDto
//                    );
//                    System.out.println("✍️ إرسال تحديث الحالة " + userDto.isOnline()
//                            + " لجهة الاتصال: " + participant + " at " + new Date());
//                })
//        );
//    }
//
//
//    /** التعامل مع الاتصال/القطع (مثلاً عند CONNECT أو DISCONNECT) */
//    @Override
//    public void handleConnectionEvent(String phoneNumber, boolean isConnected) {
//        updateUserPresence(phoneNumber, isConnected);
//        if (isConnected) {
//            lastHeartbeatMap.put(phoneNumber, System.currentTimeMillis());
//            // إنشاء NotificationEvent بدلاً من إرسال String مباشرة
//            NotificationEvent event = NotificationEvent.builder()
//                    .notificationId(UUID.randomUUID().toString())
//                    .userId(phoneNumber)
//                    .eventType(NotificationEvent.EventType.SYSTEM)
//                    .systemMessage("CONNECTION_EVENT")
//                    .timestamp(LocalDateTime.now())
//                    .build();
//            kafkaTemplate.send(CONNECTION_EVENTS_TOPIC, phoneNumber, event);
////            kafkaTemplate.send(CONNECTION_EVENTS_TOPIC, phoneNumber, phoneNumber);
//
//        } else {
//            lastHeartbeatMap.remove(phoneNumber);
//        }
//    }
//    public boolean isUserConnected(String userId) {
//        return lastHeartbeatMap.containsKey(userId);
//    }
//    /** غير مستخدم حالياً */
//    @Override
//    public void updateLastActivity(String phoneNumber) {
//        // intentionally left blank
//    }
//
//
//    @Scheduled(fixedRate = 30000) // كل 30 ثانية
//    public void checkHeartbeats() {
//        long now = System.currentTimeMillis();
//        lastHeartbeatMap.forEach((phone, lastBeat) -> {
//            if (now - lastBeat > HEARTBEAT_TIMEOUT) {
//                User user = userRepository.findByPhoneNumber(phone)
//                        .orElse(null);
//
//                if (user != null && user.isOnline()) {
//                    user.setOnline(false);
//                    user.setLastSeen(LocalDateTime.now());
//                    userRepository.save(user);
//                    notifyPresenceToContacts(phone, false);
//                    lastHeartbeatMap.remove(phone);
//                    System.out.println("✍️ تم تعيين المستخدم غير متصل: " + phone);
//                }
//            }
//        });
//    }
//    public void handleHeartbeat(String phoneNumber) {
//        lastHeartbeatMap.put(phoneNumber, System.currentTimeMillis());
//    }
//
//
//}
