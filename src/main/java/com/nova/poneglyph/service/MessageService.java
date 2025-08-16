package com.nova.poneglyph.service;

import com.nova.poneglyph.dto.MessageDTO;
import com.nova.poneglyph.dto.SendMessageRequest;
import com.nova.poneglyph.enums.MessageStatus;
import com.nova.poneglyph.events.ConversationUpdateEvent;
import com.nova.poneglyph.model.Conversation;
import com.nova.poneglyph.model.Message;
import com.nova.poneglyph.notification.NotificationService;
import com.nova.poneglyph.repository.ConversationRepository;
import com.nova.poneglyph.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final NotificationService notificationService; // ✅ أضفنا NotificationService
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
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
        Conversation savedConversation = conversationRepository.save(conversation);

        // إنشاء DTO للرسالة
        MessageDTO messageDTO = convertToDTO(message);

        // إرسال الرسالة عبر NotificationService (Kafka + WebSocket)
        notificationService.sendMessageNotification(request.getReceiverPhone(), messageDTO);

        // إرسال تحديث المحادثة لجميع المشاركين
        ConversationUpdateEvent updateEvent = new ConversationUpdateEvent();
        updateEvent.setConversationId(savedConversation.getId());
        updateEvent.setLastMessage(messageDTO);
        updateEvent.setUnreadCount(calculateUnreadCount(savedConversation, request.getReceiverPhone()));
        updateEvent.setParticipantIds(savedConversation.getParticipantIds());

        for (String participant : savedConversation.getParticipantIds()) {
            notificationService.sendConversationUpdate(participant, updateEvent);
        }

        return messageDTO;
    }

    @Transactional
    public void markAsDelivered(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getDeliveredAt() == null) {
            message.setDeliveredAt(LocalDateTime.now());
            message.setStatus(MessageStatus.DELIVERED);
            messageRepository.save(message);

            MessageDTO messageDTO = convertToDTO(message);

            for (String participant : message.getConversation().getParticipantIds()) {
                notificationService.sendMessageNotification(participant, messageDTO);
            }
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
            messageRepository.save(message);

            MessageDTO messageDTO = convertToDTO(message);

            for (String participant : message.getConversation().getParticipantIds()) {
                notificationService.sendMessageNotification(participant, messageDTO);
            }
        }
    }

    private int calculateUnreadCount(Conversation conversation, String senderPhone) {
        return (int) conversation.getMessages().stream()
                .filter(msg -> !msg.getSenderPhone().equals(senderPhone))
                .filter(msg -> msg.getStatus() != MessageStatus.SEEN)
                .count();
    }

    public MessageDTO convertToDTO(Message message) {
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


}
