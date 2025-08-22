//package com.nova.poneglyph.service;
//
//
//
//import com.nova.poneglyph.domain.conversation.Conversation;
//import com.nova.poneglyph.domain.message.Message;
//import com.nova.poneglyph.domain.message.MessageStatus;
//import com.nova.poneglyph.domain.enums.DeliveryStatus;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.dto.MessageSendDto;
//import com.nova.poneglyph.exception.MessageException;
//import com.nova.poneglyph.repository.ConversationRepository;
//import com.nova.poneglyph.repository.MessageRepository;
//import com.nova.poneglyph.repository.MessageStatusRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.util.EncryptionUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class MessageService {
//
//    private final MessageRepository messageRepository;
//    private final MessageStatusRepository statusRepository;
//    private final ConversationRepository conversationRepository;
//    private final UserRepository userRepository;
//    private final WebSocketService webSocketService;
//
//    @Transactional
//    public void sendMessage(UUID conversationId, UUID senderId, MessageSendDto dto) {
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new MessageException("Conversation not found"));
//
//        User sender = userRepository.findById(senderId)
//                .orElseThrow(() -> new MessageException("User not found"));
//
//        // Encrypt message content
//        String encryptedContent = dto.isEncrypt() ?
//                EncryptionUtil.encrypt(dto.getContent(), conversation.getEncryptionKey()) :
//                dto.getContent();
//
//        Message message = Message.builder()
//                .conversation(conversation)
//                .sender(sender)
//                .messageType(dto.getMessageType())
//                .encryptedContent(encryptedContent.getBytes())
//                .contentHash(EncryptionUtil.hash(dto.getContent()))
//                .build();
//
//        message = messageRepository.save(message);
//
//        // Update conversation last message time
//        conversation.setLastMessageAt(OffsetDateTime.now());
//        conversationRepository.save(conversation);
//
//        // Create initial message statuses
//        createInitialStatuses(message, conversation);
//
//        // Notify participants via WebSocket
//        webSocketService.notifyNewMessage(conversationId, message);
//    }
//
//    private void createInitialStatuses(Message message, Conversation conversation) {
//        List<Participant> participants = participantRepository.findByConversation(conversation);
//
//        for (Participant participant : participants) {
//            DeliveryStatus status = participant.getUser().getId().equals(message.getSender().getId()) ?
//                    DeliveryStatus.SENT : DeliveryStatus.SENT;
//
//            MessageStatus msgStatus = MessageStatus.builder()
//                    .message(message)
//                    .user(participant.getUser())
//                    .status(status)
//                    .updatedAt(OffsetDateTime.now())
//                    .build();
//
//            statusRepository.save(msgStatus);
//        }
//    }
//
//    @Transactional
//    public void markAsDelivered(UUID messageId, UUID userId) {
//        updateMessageStatus(messageId, userId, DeliveryStatus.DELIVERED);
//    }
//
//    @Transactional
//    public void markAsRead(UUID messageId, UUID userId) {
//        updateMessageStatus(messageId, userId, DeliveryStatus.READ);
//    }
//
//    private void updateMessageStatus(UUID messageId, UUID userId, DeliveryStatus status) {
//        MessageStatus.Id id = new MessageStatus.Id(messageId, userId);
//        statusRepository.findById(id).ifPresent(msgStatus -> {
//            msgStatus.setStatus(status);
//            msgStatus.setUpdatedAt(OffsetDateTime.now());
//            statusRepository.save(msgStatus);
//        });
//    }
//}
