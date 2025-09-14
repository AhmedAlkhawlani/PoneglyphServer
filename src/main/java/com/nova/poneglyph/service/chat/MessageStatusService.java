package com.nova.poneglyph.service.chat;

import com.nova.poneglyph.domain.conversation.Participant;
import com.nova.poneglyph.domain.enums.DeliveryStatus;
import com.nova.poneglyph.domain.message.Message;
import com.nova.poneglyph.domain.message.MessageStatus;
import com.nova.poneglyph.dto.conversation.MessageDTO;
import com.nova.poneglyph.repository.MessageRepository;
import com.nova.poneglyph.repository.MessageStatusRepository;
import com.nova.poneglyph.repository.ParticipantRepository;
import com.nova.poneglyph.service.WebSocketService;
import com.nova.poneglyph.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageStatusService {

    private final MessageStatusRepository messageStatusRepository;
    private final ParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final WebSocketService socketService;
//    private final MessageService messageService;
    private final EncryptionUtil encryptionUtil;
    private final Logger log = LoggerFactory.getLogger(MessageStatusService.class);
    @Transactional
    public void createStatusesForMessage(Message message) {
        OffsetDateTime now = OffsetDateTime.now();

        List<Participant> participants = participantRepository.findByConversation(message.getConversation());

        List<MessageStatus> statuses = participants.stream().map(p -> {
            DeliveryStatus st = p.getUser().getId().equals(message.getSender().getId()) ? DeliveryStatus.READ : DeliveryStatus.SENT;
            return MessageStatus.builder()
                    .message(message)
                    .user(p.getUser())
                    .status(st)
                    .updatedAt(now)
                    .build();
        }).collect(Collectors.toList());

        messageStatusRepository.saveAll(statuses);

        // بَث الرسالة مرة واحدة لقناة المحادثة
        MessageDTO dto = convertToDto(message);
        try {
            socketService.notifyNewMessage(message.getConversation().getId(), dto);
        } catch (Throwable ignored) {}

        // بَث حالة SENT لكل مستلم (except sender)
        statuses.stream()
                .filter(ms -> !ms.getUser().getId().equals(message.getSender().getId()))
                .forEach(ms -> {
                    try {
                        socketService.notifyMessageDeliveryStatus(message.getId(), message.getConversation().getId(), ms.getUser().getId(), DeliveryStatus.SENT);
                    } catch (Throwable ignored) {}
                });
    }

    @Transactional
    public void markDelivered(UUID userId, UUID messageId) {
        OffsetDateTime now = OffsetDateTime.now();
        int updated = messageStatusRepository.updateStatusForUser(messageId, userId, DeliveryStatus.DELIVERED, now);
        if (updated > 0) {
            messageRepository.findById(messageId).ifPresent(msg -> {
                try {
                    socketService.notifyMessageDeliveryStatus(messageId, msg.getConversation().getId(), userId, DeliveryStatus.DELIVERED);
                } catch (Throwable ignored) {}
            });
        }
    }
    @Transactional
    public void markMessageAsRead(UUID userId, UUID messageId) {
        OffsetDateTime now = OffsetDateTime.now();
        int updated = messageStatusRepository.updateStatusForUser(messageId, userId, DeliveryStatus.READ, now);
        if (updated > 0) {
            messageRepository.findById(messageId).ifPresent(msg -> {
                try {
                    socketService.notifyMessageReadStatus(messageId, msg.getConversation().getId(), userId, DeliveryStatus.READ);
                } catch (Throwable ignored) {}
            });
        }
    }

//    @Transactional
//    public int markRead(UUID userId, UUID conversationId) {
//        OffsetDateTime now = OffsetDateTime.now();
//        int updated = messageStatusRepository.markAllInConversationForUser(conversationId, userId, DeliveryStatus.READ, now);
//        try {
//            socketService.notifyMessageDeliveryStatus(null, conversationId, userId, DeliveryStatus.READ);
//        } catch (Throwable ignored) {}
//        return updated;
//    }

    @Transactional
    public int markRead(UUID userId, UUID conversationId) {
        OffsetDateTime now = OffsetDateTime.now();
        int updated = messageStatusRepository.markAllInConversationForUser(conversationId, userId, DeliveryStatus.READ, now);

        try {
            // إضافة logging لتتبع العملية
            log.debug("Marked {} messages as READ in conversation {} for user {}", updated, conversationId, userId);

            // إرسال إشعار بأن المحادثة كاملة تم قراءتها (messageId = null)
            socketService.notifyMessageDeliveryStatus(null, conversationId, userId, DeliveryStatus.READ);

        } catch (Throwable t) {
            log.warn("Failed to notify READ status for conversation {}: {}", conversationId, t.getMessage());
        }

        return updated;
    }

    @Transactional
    public void markDeleted(UUID messageId, UUID requesterUserId, boolean forEveryone) {
        OffsetDateTime now = OffsetDateTime.now();
        Message msg = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));

        if (forEveryone) {
            msg.setDeletedForAll(true);
            msg.setDeletedAt(now);
            messageRepository.save(msg);

            List<Participant> parts = participantRepository.findByConversation(msg.getConversation());
            for (Participant p : parts) {
                try {
                    messageStatusRepository.updateStatusForUser(messageId, p.getUser().getId(), DeliveryStatus.DELETED, now);
                } catch (Throwable ignored) {}
            }

            try {
                socketService.notifyMessageDeleted(messageId, true);
            } catch (Throwable ignored) {}

        } else {
            messageStatusRepository.updateStatusForUser(messageId, requesterUserId, DeliveryStatus.DELETED, now);
            try {
                socketService.notifyMessageDeliveryStatus(messageId, msg.getConversation().getId(), requesterUserId, DeliveryStatus.DELETED);
            } catch (Throwable ignored) {}
        }
    }

    private MessageDTO convertToDto(Message message) {
        try {
//            return messageService.convertToDto(message);
            MessageDTO dto = new MessageDTO();
            dto.setId(message.getId());
            dto.setConversationId(message.getConversation() != null ? message.getConversation().getId() : null);
            dto.setSenderId(message.getSender() != null ? message.getSender().getId() : null);
            dto.setSenderPhone(message.getSender() != null ? message.getSender().getPhoneNumber() : null);
            dto.setSenderName(message.getSender() != null ? message.getSender().getDisplayName() : null);
            dto.setMessageType(message.getMessageType() != null ? message.getMessageType().name() : null);

            try {
                String decrypted = encryptionUtil.decryptFromBytes(message.getEncryptedContent(), message.getConversation().getEncryptionKey());
                dto.setContent(decrypted);
            } catch (Throwable t) {
                dto.setContent(null);
            }

            dto.setCreatedAt(message.getCreatedAt());
            dto.setSequenceNumber(message.getSequenceNumber());
            dto.setLocalId(message.getLocalId());
            return dto;
        } catch (Exception e) {
            // Fallback conversion if main service fails
            MessageDTO dto = new MessageDTO();
            dto.setId(message.getId());
            dto.setConversationId(message.getConversation().getId());
            dto.setSenderId(message.getSender().getId());
            dto.setCreatedAt(message.getCreatedAt());
            dto.setMessageType(message.getMessageType().name());

            // حاول فك التشفير يدوياً إذا لزم الأمر
            try {
                String decrypted = encryptionUtil.decryptFromBytes(
                        message.getEncryptedContent(),
                        message.getConversation().getEncryptionKey()
                );
                dto.setContent(decrypted);
            } catch (Exception decryptionException) {
                dto.setContent("[Unable to decrypt message]");
            }
            return dto;
        }
    }
}
