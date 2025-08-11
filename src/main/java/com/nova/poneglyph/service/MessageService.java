package com.nova.poneglyph.service;

import com.nova.poneglyph.dto.ChatMessage;
import com.nova.poneglyph.dto.MessageDTO;
import com.nova.poneglyph.dto.Notification;
import com.nova.poneglyph.dto.SendMessageRequest;
import com.nova.poneglyph.enums.MessageStatus;
import com.nova.poneglyph.events.ConversationUpdateEvent;
import com.nova.poneglyph.model.Conversation;
import com.nova.poneglyph.model.Message;
import com.nova.poneglyph.repository.ConversationRepository;
import com.nova.poneglyph.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;
//    private final MessageMapper messageMapper;

    @Transactional
//    @Transactional
    public MessageDTO sendMessage(SendMessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Message message = Message.builder()
                .conversation(conversation)
                .senderPhone(request.getSenderPhone())
                .content(request.getContent())
                .status(MessageStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

        message = messageRepository.save(message);

        // تحديث المحادثة
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation savedConversation =conversationRepository.save(conversation);

        // إنشاء DTO للرسالة
        MessageDTO messageDTO = new MessageDTO(
                message.getId(),
                message.getConversation().getId(),
                message.getSenderPhone(),
                message.getContent(),
                message.getSentAt(),
                message.getDeliveredAt(),
                message.getSeenAt(),
                message.getStatus(),
                null,
                null

        );
//        MessageDTO messageDTO = messageMapper.toDTO(message);

        // إرسال الرسالة للمستقبل
        messagingTemplate.convertAndSendToUser(
                request.getReceiverPhone(),
                "/queue/messages",
                messageDTO
        );

        // إرسال تحديث المحادثة لجميع المشاركين
        ConversationUpdateEvent updateEvent = new ConversationUpdateEvent();
        updateEvent.setConversationId(savedConversation.getId());
        updateEvent.setLastMessage(messageDTO);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserPhone =auth.getName();

        // احسب عدد الرسائل غير المقروءة
        int unreadCount = (int) savedConversation.getMessages().stream()
                .filter(msg -> !msg.getSenderPhone().equals(request.getReceiverPhone())) // ليست من المرسل الحالي
                .filter(msg -> msg.getStatus() != MessageStatus.SEEN)          // لم تُشاهد بعد
                .count();
        updateEvent.setUnreadCount(calculateUnreadCount(savedConversation, request.getReceiverPhone()));
        updateEvent.setParticipantIds(savedConversation.getParticipantIds()); // إضافة هذا السطر

        for (String participant : savedConversation.getParticipantIds()) {
            messagingTemplate.convertAndSendToUser(
                    participant,
                    "/queue/conversation-updates",
                    updateEvent
            );
        }

        return messageDTO;
    }

    //    public MessageDTO sendMessage(SendMessageRequest request) {
//        Conversation conversation = conversationRepository.findById(request.getConversationId())
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        Message message = Message.builder()
//                .conversation(conversation)
//                .senderPhone(request.getSenderPhone())
//                .content(request.getContent())
//                .status(MessageStatus.SENT)
//                .sentAt(LocalDateTime.now())
//                .build();
//
//        message = messageRepository.save(message);
//
//        // إرسال الرسالة عبر WebSocket
//        MessageDTO messageDTO = convertToDTO(message);
//        messagingTemplate.convertAndSendToUser(
//                request.getReceiverPhone(),
//                "/queue/messages",
//                messageDTO
//        );
//
//        // إرسال تحديث المحادثة لجميع المشاركين
//        ConversationUpdateEvent updateEvent = new ConversationUpdateEvent();
//        updateEvent.setConversationId(conversation.getId());
//        updateEvent.setLastMessage(messageDTO);
//        updateEvent.setUnreadCount(calculateUnreadCount(conversation, request.getSenderPhone()));
//
//        for (String participant : conversation.getParticipantIds()) {
//            messagingTemplate.convertAndSendToUser(
//                    participant,
//                    "/queue/conversation-updates",
//                    updateEvent
//            );
//        }
//        //  --> هنا ترسل إشعار لكافكا
//        Notification notification = Notification.builder()
//                .notificationId(UUID.randomUUID().toString())
//                .userId(message.getConversation().getId())  // أو userId المناسب هنا — عادة هو receiver phone
//                .message("New message from " + message.getSenderPhone())
//                .timestamp(LocalDateTime.now())
//                .type(Notification.NotificationType.MESSAGE)
//                .relatedMessageId(message.getId())
//                .build();
//        chatService.sendMessageNotificationFromMessage(notification);
//
//        return messageDTO;
//    }

    private int calculateUnreadCount(Conversation conversation, String senderPhone) {
        return (int) conversation.getMessages().stream()
                .filter(msg -> !msg.getSenderPhone().equals(senderPhone))
                .filter(msg -> msg.getStatus() != MessageStatus.SEEN)
                .count();
    }
    @Transactional
    public void markAsDelivered(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getDeliveredAt() == null) {
            message.setDeliveredAt(LocalDateTime.now());
            message.setStatus(MessageStatus.DELIVERED);
            message = messageRepository.save(message);

            // إرسال حدث التحديث عبر WebSocket
            MessageDTO messageDTO = convertToDTO(message);


            for (String participant : message.getConversation().getParticipantIds()) {
                messagingTemplate.convertAndSendToUser(
                        participant,
                        "/queue/messages",
                        messageDTO
                );
            }
//            messagingTemplate.convertAndSend(
//                    "/topic/conversation/"+message.getConversation().getId(),
//                    messageDTO);
//            MessageDTO messageDTO = convertToDTO(message);
//            messagingTemplate.convertAndSend(
//                    "/topic/conversation/"+message.getConversation().getId(),
//                    messageDTO);
        }
    }

    @Transactional
    public void markAsSeen(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getSeenAt() == null) {
            message.setSeenAt(LocalDateTime.now());
            message.setStatus(MessageStatus.SEEN);
            if (message.getDeliveredAt() == null) {
                message.setDeliveredAt(LocalDateTime.now());
            }
            message = messageRepository.save(message);

            // إرسال حدث التحديث عبر WebSocket
            MessageDTO messageDTO = convertToDTO(message);

            for (String participant : message.getConversation().getParticipantIds()) {
                messagingTemplate.convertAndSendToUser(
                        participant,
                        "/queue/messages",
                        messageDTO
                );
            }
            // إرسال الرسالة للمستقبل
//            messagingTemplate.convertAndSendToUser(
//                    message.getConversation()...getReceiverPhone(),
//                    "/queue/messages",
//                    messageDTO
//            );
//            messagingTemplate.convertAndSend(
//                    "/topic/conversation/"+message.getConversation().getId(),
//                    messageDTO);
        }
    }

    @Transactional
    public List<MessageDTO> getMessagesByConversation(String conversationId, int page, int size) {
        Page<Message> messages = messageRepository.findByConversationId(
                conversationId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt")));

        return messages.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
//    @Transactional
//    public void markAsSeen(String messageId) {
//        Message message = messageRepository.findById(messageId)
//                .orElseThrow(() -> new RuntimeException("Message not found"));
//
//        if (message.getSeenAt() == null) {
//            message.setSeenAt(LocalDateTime.now());
//            message.setStatus(MessageStatus.SEEN);
//            messageRepository.save(message);
//        }
//    }

    MessageDTO convertToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderPhone(message.getSenderPhone())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .seenAt(message.getSeenAt())
                .status(message.getStatus())
                .build();
    }
    private ChatMessage toChatMessage(Message message) {
        return ChatMessage.builder()
                .messageId(message.getId())
                .senderId(message.getSenderPhone())

//                .receiverId(message.getConversation().getParticipants().stream()
//                        .filter(p -> !p.equals(message.getSenderPhone()))
//                        .findFirst()
//                        .orElse(null))  // أو حسب كيفية تخزين المستلمين
                .content(message.getContent())
                .timestamp(message.getSentAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .status(ChatMessage.MessageStatus.SENT)
                .build();
    }

}
