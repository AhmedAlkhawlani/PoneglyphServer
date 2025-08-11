//package com.nova.poneglyph.service;
//
//import com.nova.poneglyph.config.PresenceNotifier;
//import com.nova.poneglyph.dto.PresenceDTO;
//import com.nova.poneglyph.dto.TypingEventRequest;
//import com.nova.poneglyph.events.TypingEvent;
//import com.nova.poneglyph.mapper.UserMapper;
//import com.nova.poneglyph.model.Conversation;
//import com.nova.poneglyph.model.User;
//import com.nova.poneglyph.repository.ConversationRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.user.dto.UserDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//@RequiredArgsConstructor
//public class PresenceService implements PresenceNotifier {
//    private final UserRepository userRepository;
//    private final ConversationRepository conversationRepository;
//
//    private final ApplicationContext applicationContext;
//
//    private SimpMessagingTemplate getMessagingTemplate() {
//        return applicationContext.getBean(SimpMessagingTemplate.class);
//    }
//    public void handleTypingEvent(TypingEventRequest request) {
//        SimpMessagingTemplate messagingTemplate = getMessagingTemplate();
//
//        TypingEvent typingEvent=new TypingEvent(
//                request.getConversationId(),
//                request.getSenderPhone(),
//                request.getReceiverPhone(),
//                request.isTyping()
//        );
//
//        // إرسال حدث الكتابة للمستلم عبر WebSocket
//        messagingTemplate.convertAndSendToUser(
//                request.getReceiverPhone(),
//                "/queue/typing",
//                typingEvent
//        );
//
//    }
//
//    public PresenceDTO getUserPresence(String phoneNumber) {
//        User user = userRepository.findByPhoneNumber(phoneNumber)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//
//        return PresenceDTO.builder()
//                .phoneNumber(phoneNumber)
//                .online(user.isOnline())
//                .lastSeen(user.getLastSeen())
//                .status(user.isOnline() ? "online" : "offline")
//                .build();
//    }
//
////    public void updateUserPresence(String phoneNumber, boolean isOnline) {
////        User user = userRepository.findByPhoneNumber(phoneNumber)
////                .orElseThrow(() -> new RuntimeException("User not found"));
////
////        user.setOnline(isOnline);
////        user.setLastSeen(isOnline ? null : LocalDateTime.now());
////        userRepository.save(user);
////
////    }
//
//    public void updateUserPresence(String phoneNumber, boolean isOnline) {
//        User user = userRepository.findByPhoneNumber(phoneNumber)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        user.setOnline(isOnline);
//        user.setLastSeen(isOnline ? null : LocalDateTime.now());
//        userRepository.save(user);
//
//        // إرسال تحديث الحالة لجميع المستخدمين المتصلين به
//        notifyPresenceToContacts(phoneNumber, isOnline);
//    }
//
//    private void notifyPresenceToContacts(String phoneNumber, boolean isOnline) {
//        SimpMessagingTemplate messagingTemplate = getMessagingTemplate();
//
//        // الحصول على جميع المحادثات التي يشارك فيها المستخدم
//        List<Conversation> conversations = conversationRepository
//                .findByParticipantPhonesContaining(phoneNumber);
//
//        // تحويل إلى DTO وإرسالها
//        UserDto userDto = UserMapper.toDTO(
//                userRepository.findByPhoneNumber(phoneNumber).orElseThrow(),
//                false
//        );
//
//        conversations.forEach(conversation -> {
//            conversation.getParticipantIds().stream()
//                    .filter(participant -> !participant.equals(phoneNumber))
//                    .forEach(participant -> {
//                        // إرسال التحديث لكل مشارك في المحادثة
//                        messagingTemplate.convertAndSendToUser(
//                                participant,
//                                "/queue/presence",
//                                userDto
//                        );
//                    System.out.println("✍️رسال تحديث الحالة "+ userDto.isOnline()+ "لجميع جهات الاتصال [checkDisconnectedUsers]  at "+participant + new Date());
//
//
//                    });
//        });
//    }
//
//
//    private final Map<String, LocalDateTime> lastSeenMap = new ConcurrentHashMap<>();
//
////    @Scheduled(fixedRate = 5000) // كل 30 ثانية
//    @Scheduled(fixedRate = 30000) // كل 30 ثانية
//    public void checkDisconnectedUsers() {
//        userRepository.findAll().forEach(user -> {
//            if(user.isOnline()) {
//                LocalDateTime lastActive = lastSeenMap.get(user.getPhoneNumber());
//                if(lastActive != null && lastActive.isBefore(LocalDateTime.now().minusSeconds(40))) {
//                    user.setOnline(false);
//                    user.setLastSeen(LocalDateTime.now());
//                    userRepository.save(user);
//
//                    // إرسال تحديث الحالة لجميع جهات الاتصال
//                    notifyContacts(user.getPhoneNumber(), false);
//                    System.out.println("✍️رسال تحديث الحالة false لجميع جهات الاتصال [checkDisconnectedUsers]  at "+user.getPhoneNumber() + new Date());
//
//                }
//            }
//        });
//                System.out.println("✍️ [checkDisconnectedUsers]  at " + new Date());
//
//    }
//
//    public void handleConnectionEvent(String phoneNumber, boolean isConnected) {
//        User user = userRepository.findByPhoneNumber(phoneNumber)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if(isConnected) {
//            user.setOnline(true);
//            user.setLastSeen(null);
//        } else {
//            user.setOnline(false);
//            user.setLastSeen(LocalDateTime.now());
//        }
//
//        userRepository.save(user);
//        lastSeenMap.put(phoneNumber, LocalDateTime.now());
//        notifyContacts(phoneNumber, isConnected);
//    }
//
//    private void notifyContacts(String phoneNumber, boolean isOnline) {
//        SimpMessagingTemplate messagingTemplate = getMessagingTemplate();
//
//        UserDto userDto = UserMapper.toDTO(userRepository.findByPhoneNumber(phoneNumber).orElseThrow(), false);
//
//        messagingTemplate.convertAndSend("/topic/presence/" + phoneNumber, userDto);
//        System.out.println(" تحديث الحالة  [notifyContacts]  at " + new Date());
//
//        System.out.println("✍️ [notifyContacts]  at "+"/topic/presence/" + phoneNumber + new Date());
//
//    }
//
//    private final Map<String, LocalDateTime> lastActivityMap = new ConcurrentHashMap<>();
//
////    @Scheduled(fixedRate = 5000) // كل 30 ثانية
//    @Scheduled(fixedRate = 30000) // كل 30 ثانية
//    public void checkInactiveUsers() {
//        userRepository.findAll().forEach(user -> {
//            if (user.isOnline()) {
//                LocalDateTime lastActivity = lastActivityMap.get(user.getPhoneNumber());
//                if (lastActivity != null && lastActivity.isBefore(LocalDateTime.now().minusSeconds(45))) {
//                    handleConnectionEvent(user.getPhoneNumber(), false);
//                    System.out.println("✍️رسال تحديث الحالة false لجميع جهات الاتصال [checkInactiveUsers]  at " +user.getPhoneNumber()+ new Date());
//
//                }
//            }
//        });
//
//
//        System.out.println("✍️ [checkInactiveUsers]  at " + new Date());
//
//
//    }
//
//    public void updateLastActivity(String phoneNumber) {
//        lastActivityMap.put(phoneNumber, LocalDateTime.now());
//    }
//
//
//    private final Map<String, Long> lastHeartbeatMap = new ConcurrentHashMap<>();
//    private static final long HEARTBEAT_TIMEOUT = 40000; // 40 ثانية
//
////    @Scheduled(fixedRate = 5000) // كل 30 ثانية
//    @Scheduled(fixedRate = 30000) // كل 30 ثانية
//    public void checkHeartbeats() {
//        lastHeartbeatMap.forEach((phone, timestamp) -> {
//            if (System.currentTimeMillis() - timestamp > HEARTBEAT_TIMEOUT) {
//                handleConnectionEvent(phone, false);
//                lastHeartbeatMap.remove(phone);
//                System.out.println("✍️رسال تحديث  [checkHeartbeats]  at " + phone+new Date());
//
//            }
//        });
//
//        System.out.println("✍️ [checkHeartbeats]  at " + new Date());
//
//    }
//
//    public void handleHeartbeat(String phoneNumber) {
//        lastHeartbeatMap.put(phoneNumber, System.currentTimeMillis());
//    }
//}
package com.nova.poneglyph.service;

import com.nova.poneglyph.config.PresenceNotifier;
import com.nova.poneglyph.dto.PresenceDTO;
import com.nova.poneglyph.dto.TypingEventRequest;
import com.nova.poneglyph.events.TypingEvent;
import com.nova.poneglyph.mapper.UserMapper;
import com.nova.poneglyph.model.Conversation;
import com.nova.poneglyph.model.User;
import com.nova.poneglyph.repository.ConversationRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.user.dto.UserDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class PresenceService implements PresenceNotifier {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ApplicationContext applicationContext;

    private SimpMessagingTemplate getMessagingTemplate() {
        return applicationContext.getBean(SimpMessagingTemplate.class);
    }

    /** حدث الكتابة */
    public void handleTypingEvent(TypingEventRequest request) {
        TypingEvent typingEvent = new TypingEvent(
                request.getConversationId(),
                request.getSenderPhone(),
                request.getReceiverPhone(),
                request.isTyping()
        );
        getMessagingTemplate().convertAndSendToUser(
                request.getReceiverPhone(),
                "/queue/typing",
                typingEvent
        );
    }

    /** الحصول على حالة المستخدم */
    public PresenceDTO getUserPresence(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return PresenceDTO.builder()
                .phoneNumber(phoneNumber)
                .online(user.isOnline())
                .lastSeen(user.getLastSeen())
                .status(user.isOnline() ? "online" : "offline")
                .build();
    }

    /** تحديث حالة المستخدم وإشعار جهات الاتصال */
    public void updateUserPresence(String phoneNumber, boolean isOnline) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setOnline(isOnline);
        user.setLastSeen(isOnline ? null : LocalDateTime.now());
        userRepository.save(user);

        notifyPresenceToContacts(phoneNumber, isOnline);
    }

    /** إشعار جهات الاتصال بتغير حالة المستخدم */
    private void notifyPresenceToContacts(String phoneNumber, boolean isOnline) {
        List<Conversation> conversations = conversationRepository
                .findByParticipantPhonesContaining(phoneNumber);

        UserDto userDto = UserMapper.toDTO(
                userRepository.findByPhoneNumber(phoneNumber).orElseThrow(),
                false
        );

        conversations.forEach(conversation -> conversation.getParticipantIds().stream()
                .filter(participant -> !participant.equals(phoneNumber))
                .forEach(participant -> {
                    getMessagingTemplate().convertAndSendToUser(
                            participant,
                            "/queue/presence",
                            userDto
                    );
                    System.out.println("✍️ إرسال تحديث الحالة " + userDto.isOnline()
                            + " لجهة الاتصال: " + participant + " at " + new Date());
                })
        );
    }


    /** التعامل مع الاتصال/القطع (مثلاً عند CONNECT أو DISCONNECT) */
    @Override
    public void handleConnectionEvent(String phoneNumber, boolean isConnected) {
        updateUserPresence(phoneNumber, isConnected);
        if (isConnected) {
            lastHeartbeatMap.put(phoneNumber, System.currentTimeMillis());
        } else {
            lastHeartbeatMap.remove(phoneNumber);
        }
    }

    /** غير مستخدم حالياً */
    @Override
    public void updateLastActivity(String phoneNumber) {
        // intentionally left blank
    }
//private final Map<String, Long> lastHeartbeatMap = new ConcurrentHashMap<>();
//    private static final long HEARTBEAT_TIMEOUT = 40000; // 40 ثانية

//    @Scheduled(fixedRate = 30000) // كل 30 ثانية
//    public void checkHeartbeats() {
//        long now = System.currentTimeMillis();
//        userRepository.findAll().forEach(user -> {
//            String phone = user.getPhoneNumber();
//            Long lastHeartbeat = lastHeartbeatMap.get(phone);
//            if (lastHeartbeat == null || now - lastHeartbeat > HEARTBEAT_TIMEOUT) {
//                if (user.isOnline()) {
//                    user.setOnline(false);
//                    user.setLastSeen(LocalDateTime.now());
//                    userRepository.save(user);
//                    notifyPresenceToContacts(phone, false);
//                    lastHeartbeatMap.remove(phone);
//                    System.out.println("✍️ تم تعيين المستخدم غير متصل بسبب عدم استقبال نبضة heartbeat: " + phone);
//                }
//            } else {
//                // المستخدم نشط، تأكد أنه متصل
//                if (!user.isOnline()) {
//                    user.setOnline(true);
//                    user.setLastSeen(null);
//                    userRepository.save(user);
//                    notifyPresenceToContacts(phone, true);
//                    System.out.println("✍️ تم تعيين المستخدم متصل بسبب استقبال نبضة heartbeat: " + phone);
//                }
//            }
//        });
//    }
private final Map<String, Long> lastHeartbeatMap = new ConcurrentHashMap<>();
    private static final long HEARTBEAT_TIMEOUT = 60000; // 60 ثانية

    @Scheduled(fixedRate = 30000) // كل 30 ثانية
    public void checkHeartbeats() {
        long now = System.currentTimeMillis();
        lastHeartbeatMap.forEach((phone, lastBeat) -> {
            if (now - lastBeat > HEARTBEAT_TIMEOUT) {
                User user = userRepository.findByPhoneNumber(phone)
                        .orElse(null);

                if (user != null && user.isOnline()) {
                    user.setOnline(false);
                    user.setLastSeen(LocalDateTime.now());
                    userRepository.save(user);
                    notifyPresenceToContacts(phone, false);
                    lastHeartbeatMap.remove(phone);
                    System.out.println("✍️ تم تعيين المستخدم غير متصل: " + phone);
                }
            }
        });
    }
    public void handleHeartbeat(String phoneNumber) {
        lastHeartbeatMap.put(phoneNumber, System.currentTimeMillis());
    }


}
