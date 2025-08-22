package com.nova.poneglyph.service.notification;



import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.NotificationDto;
import com.nova.poneglyph.dto.NotificationSettings;
import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.service.FcmService;
import com.nova.poneglyph.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class NotificationService {
//
//    private final FcmService fcmService;
//    private final WebSocketService webSocketService;
//    private final UserRepository userRepository;
//
//    public void sendNewMessageNotification(UUID conversationId, UUID senderId, UUID recipientId) {
//        User recipient = userRepository.findById(recipientId).orElseThrow();
//        User sender = userRepository.findById(senderId).orElseThrow();
//
//        if (recipient.getNotificationSettings().isMessageEnabled()) {
//            NotificationDto notification = new NotificationDto(
//                    "New Message",
//                    "New message from " + sender.getDisplayName(),
//                    Map.of("conversationId", conversationId.toString(), "type", "NEW_MESSAGE")
//            );
//
//            // Send via FCM
//            fcmService.sendNotification(recipient, notification);
//
//            // Send via WebSocket
//            webSocketService.sendNotification(recipientId, notification);
//        }
//    }
//
//    public void sendCallNotification(UUID callId, UUID callerId, UUID recipientId) {
//        User recipient = userRepository.findById(recipientId).orElseThrow();
//        User caller = userRepository.findById(callerId).orElseThrow();
//
//        if (recipient.getNotificationSettings().isCallEnabled()) {
//            NotificationDto notification = new NotificationDto(
//                    "Incoming Call",
//                    caller.getDisplayName() + " is calling you",
//                    Map.of("callId", callId.toString(), "type", "INCOMING_CALL")
//            );
//
//            // Send via FCM
//            fcmService.sendNotification(recipient, notification);
//
//            // Send via WebSocket
//            webSocketService.sendNotification(recipientId, notification);
//        }
//    }
//
//    public void sendSecurityNotification(UUID userId, String title, String message) {
//        NotificationDto notification = new NotificationDto(
//                title,
//                message,
//                Map.of("type", "SECURITY_ALERT")
//        );
//
//        // Always send security notifications
//        fcmService.sendNotification(userRepository.findById(userId).orElseThrow(), notification);
//        webSocketService.sendNotification(userId, notification);
//    }
//}
@RequiredArgsConstructor
@Service
public class NotificationService {

//    private final FcmService fcmService;
    private final WebSocketService webSocketService;
    private final UserRepository userRepository;

    public void sendNewMessageNotification(UUID conversationId, UUID senderId, UUID recipientId) {
        User recipient = userRepository.findById(recipientId).orElseThrow();
        User sender = userRepository.findById(senderId).orElseThrow();



// استخراج الإعدادات ككائن
        NotificationSettings settings = recipient.getNotificationSettingsObject();

        boolean enabled = settings != null && Boolean.TRUE.equals(settings.getMessageEnabled());

//        var settings = recipient.getNotificationSettings();
//        boolean enabled = settings != null && Boolean.TRUE.equals(settings.isMessageEnabled());
        if (!enabled) return;

        // تحقّق من توكن FCM (إن لزم)
//        if (!fcmService.hasValidToken(recipient)) {
//            // لا تمنع WebSocket حتى لو ما فيه FCM
//            webSocketService.sendNotification(recipientId, buildNewMessageDto(conversationId, sender));
//            return;
//        }

        NotificationDto notification = buildNewMessageDto(conversationId, sender);

        // إرسال مزدوج: FCM + WebSocket
//        try {
//            fcmService.sendNotification(recipient, notification);
//        } catch (Exception ex) {
//            // لا تفسد التجربة بسبب FCM
//        }
        webSocketService.sendNotification(recipientId, notification);
    }

    public void sendCallNotification(UUID callId, UUID callerId, UUID recipientId) {
        User recipient = userRepository.findById(recipientId).orElseThrow();
        User caller = userRepository.findById(callerId).orElseThrow();

        NotificationSettings settings = recipient.getNotificationSettingsObject();


//        var settings = recipient.getNotificationSettings();
        boolean enabled = settings != null && Boolean.TRUE.equals(settings.getCallEnabled());
        if (!enabled) return;

        NotificationDto notification = new NotificationDto(
                "Incoming Call",
                caller.getDisplayName() + " is calling you",
                java.util.Map.of("callId", callId.toString(), "type", "INCOMING_CALL")
        );

//        if (fcmService.hasValidToken(recipient)) {
//            try { fcmService.sendNotification(recipient, notification); } catch (Exception ignored) {}
//        }
        webSocketService.sendNotification(recipientId, notification);
    }

    public void sendSecurityNotification(UUID userId, String title, String message) {
        User user = userRepository.findById(userId).orElseThrow();
        NotificationDto notification = new NotificationDto(
                title,
                message,
                java.util.Map.of("type", "SECURITY_ALERT")
        );

//        if (fcmService.hasValidToken(user)) {
//            try { fcmService.sendNotification(user, notification); } catch (Exception ignored) {}
//        }
        webSocketService.sendNotification(userId, notification);
    }

    private NotificationDto buildNewMessageDto(UUID conversationId, User sender) {
        String senderName = sender.getDisplayName() != null ? sender.getDisplayName() : "Someone";
        return new NotificationDto(
                "New Message",
                "New message from " + senderName,
                java.util.Map.of("conversationId", conversationId.toString(), "type", "NEW_MESSAGE")
        );
    }
}
