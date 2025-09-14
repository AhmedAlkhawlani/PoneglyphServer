package com.nova.poneglyph.service.chat;

import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.conversation.Participant;
import com.nova.poneglyph.domain.enums.MessageType;
import com.nova.poneglyph.domain.message.Message;
import com.nova.poneglyph.dto.conversation.MessageDTO;
import com.nova.poneglyph.dto.conversation.SendMessageRequest;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.exception.PermissionDeniedException;
import com.nova.poneglyph.repository.ConversationRepository;
import com.nova.poneglyph.repository.MessageRepository;
import com.nova.poneglyph.repository.ParticipantRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.service.WebSocketService;
import com.nova.poneglyph.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final MessageStatusService messageStatusService;
    private final WebSocketService socketService;

    @Transactional
    public MessageDTO sendMessage(UUID senderId, UUID conversationId, SendMessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Participant participant = participantRepository.findByConversationAndUser(conversation, sender)
                .orElseThrow(() -> new PermissionDeniedException("User is not a participant in this conversation"));

        if (participant.getMuteUntil() != null && participant.getMuteUntil().isAfter(OffsetDateTime.now())) {
            throw new PermissionDeniedException("User is muted in this conversation");
        }

        if (request.getLocalId() != null) {
            var existing = messageRepository.findByLocalId(request.getLocalId());
            if (existing.isPresent()) {
                // idempotent: إرجاع الـ DTO الموجود
                return convertToDto(existing.get());
            }
        }

        Message message = new Message();
        message.setId(UUID.randomUUID());
        message.setConversation(conversation);
        message.setSender(sender);
        message.setMessageType(MessageType.valueOf(request.getMessageType()));
        message.setLocalId(request.getLocalId());
        message.setCreatedAt(OffsetDateTime.now());

        byte[] encrypted = encryptionUtil.encryptToBytes(request.getContent(), conversation.getEncryptionKey());
        message.setEncryptedContent(encrypted);
        message.setContentHash(encryptionUtil.hash(request.getContent()));

        messageRepository.save(message);

        conversation.setLastMessageAt(OffsetDateTime.now());
        conversationRepository.save(conversation);

        try {
            messageStatusService.createStatusesForMessage(message);
        } catch (Throwable t) {
            // لا تفشل العملية الأساسية بسبب فشل وضع حالات الرسائل
        }

        MessageDTO dto = convertToDto(message);

        // إرسال إشعار بالرسالة الجديدة
        try {
            socketService.notifyNewMessage(conversationId, dto);
        } catch (Throwable ignored) {}

        return dto;
    }

    @Transactional(readOnly = true)
    public MessageDTO getMessage(UUID requesterId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        participantRepository.findByConversationAndUser_Id(message.getConversation(), requesterId)
                .orElseThrow(() -> new PermissionDeniedException("User is not a participant in this conversation"));

        return convertToDto(message);
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(UUID requesterId, UUID conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        participantRepository.findByConversationAndUser_Id(conversation, requesterId)
                .orElseThrow(() -> new PermissionDeniedException("User is not a participant in this conversation"));

        Pageable pageable = PageRequest.of(page, size);
        var messages = messageRepository.findDistinctByConversationOrderByCreatedAtAsc(conversation, pageable);

        return messages.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public MessageDTO editMessage(UUID requesterId, UUID messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(requesterId)) {
            throw new PermissionDeniedException("Only sender can edit the message");
        }

        message.setEncryptedContent(encryptionUtil.encryptToBytes(newContent, message.getConversation().getEncryptionKey()));
        message.setContentHash(encryptionUtil.hash(newContent));
        messageRepository.save(message);

        MessageDTO dto = convertToDto(message);
        try {
            socketService.notifyMessageEdited(message.getConversation().getId(), dto);
        } catch (Throwable ignored) {}

        return dto;
    }

    @Transactional
    public void deleteMessage(UUID requesterId, UUID messageId, boolean forEveryone) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (forEveryone) {
            boolean isSender = message.getSender() != null && message.getSender().getId().equals(requesterId);
            Participant requesterParticipant = participantRepository.findByConversationAndUser_Id(message.getConversation(), requesterId).orElse(null);
            boolean isAdmin = requesterParticipant != null && requesterParticipant.getRole() != null && requesterParticipant.getRole().isAdmin();

            if (!(isSender || isAdmin)) throw new PermissionDeniedException("Insufficient privileges to delete for everyone");

            messageStatusService.markDeleted(messageId, requesterId, true);

        } else {
            messageStatusService.markDeleted(messageId, requesterId, false);
        }
    }

    public MessageDTO convertToDto(Message message) {
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
    }
}
