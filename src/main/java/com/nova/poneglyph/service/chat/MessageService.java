//package com.nova.poneglyph.service.chat;
//
//
//
//import com.nova.poneglyph.domain.conversation.Conversation;
//import com.nova.poneglyph.domain.conversation.Participant;
//import com.nova.poneglyph.domain.enums.DeliveryStatus;
//import com.nova.poneglyph.domain.message.*;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.dto.*;
//import com.nova.poneglyph.dto.chatDto.MediaAttachmentDto;
//import com.nova.poneglyph.dto.chatDto.MediaDto;
//import com.nova.poneglyph.dto.chatDto.MessageDto;
//import com.nova.poneglyph.dto.chatDto.MessageSendDto;
//import com.nova.poneglyph.exception.*;
//import com.nova.poneglyph.repository.*;
//import com.nova.poneglyph.service.WebSocketService;
//import com.nova.poneglyph.service.media.MediaService;
//import com.nova.poneglyph.util.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.time.OffsetDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class MessageService {
//
//    private final MessageRepository messageRepository;
//    private final MessageStatusRepository statusRepository;
//    private final ConversationRepository conversationRepository;
//    private final UserRepository userRepository;
//    private final EncryptionUtil encryptionUtil;
//    private final WebSocketService webSocketService;
//    private final MediaService mediaService;
//
//    @Transactional
//    public MessageDto sendMessage(UUID conversationId, UUID senderId, MessageSendDto dto) {
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new MessageException("Conversation not found"));
//
//        User sender = userRepository.findById(senderId)
//                .orElseThrow(() -> new MessageException("User not found"));
//
//        // Encrypt message content
//        String encryptedContent = dto.isEncrypt() ?
//                encryptionUtil.encrypt(dto.getContent(), conversation.getEncryptionKey()) :
//                dto.getContent();
//
//        // Create message
//        Message message = Message.builder()
//                .conversation(conversation)
//                .sender(sender)
//                .messageType(dto.getMessageType())
//                .encryptedContent(encryptedContent.getBytes())
//                .contentHash(encryptionUtil.hash(dto.getContent()))
//                .replyTo(dto.getReplyToId() != null ?
//                        messageRepository.findById(dto.getReplyToId()).orElse(null) :
//                        null)
//                .build();
//
//        message = messageRepository.save(message);
//
//        // Handle media attachments
//        if (dto.getMediaAttachments() != null && !dto.getMediaAttachments().isEmpty()) {
//            for (MediaAttachmentDto mediaDto : dto.getMediaAttachments()) {
//                mediaService.attachMediaToMessage(message, mediaDto);
//            }
//        }
//
//        // Update conversation last message time
//        conversation.setLastMessageAt(OffsetDateTime.now());
//        conversationRepository.save(conversation);
//
//        // Create initial message statuses
//        createInitialStatuses(message, conversation);
//
//        // Notify participants via WebSocket
//        webSocketService.notifyNewMessage(conversationId, convertToDto(message));
//
//        return convertToDto(message);
//    }
//
//    @Transactional
//    public void deleteMessage(UUID messageId, UUID userId, boolean forEveryone) {
//        Message message = messageRepository.findById(messageId)
//                .orElseThrow(() -> new MessageException("Message not found"));
//
//        // Verify ownership or admin rights
//        if (!message.getSender().getId().equals(userId)) {
//            Participant participant = participantRepository.findByConversationAndUser(
//                    message.getConversation(),
//                    userRepository.findById(userId).orElseThrow()
//            ).orElseThrow(() -> new PermissionDeniedException("Not in conversation"));
//
//            if (!participant.getRole().isAdmin()) {
//                throw new PermissionDeniedException("Insufficient privileges");
//            }
//        }
//
//        if (forEveryone) {
//            message.setDeletedForAll(true);
//            message.setDeletedAt(OffsetDateTime.now());
//            messageRepository.save(message);
//        } else {
//            // For self only (implement via status)
//            MessageStatus status = statusRepository.findById(
//                    new MessageStatus.Id(messageId, userId)
//            ).orElseThrow(() -> new MessageException("Status not found"));
//
//            status.setDeleted(true);
//            statusRepository.save(status);
//        }
//
//        webSocketService.notifyMessageDeleted(messageId, forEveryone);
//    }
//
//    @Transactional
//    public void addReaction(UUID messageId, UUID userId, String reaction) {
//        MessageReactionId id = new MessageReactionId(messageId, userId);
//        MessageReaction reactionEntity = reactionRepository.findById(id)
//                .orElse(new MessageReaction(id));
//
//        reactionEntity.setReaction(reaction);
//        reactionEntity.setCreatedAt(OffsetDateTime.now());
//        reactionRepository.save(reactionEntity);
//
//        webSocketService.notifyReactionUpdate(messageId, userId, reaction);
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
//                    .id(new MessageStatus.Id(message.getMessageId(), participant.getUser().getId()))
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
//    private MessageDto convertToDto(Message message) {
//        return new MessageDto(
//                message.getMessageId(),
//                message.getConversation().getConversationId(),
//                message.getSender().getId(),
//                message.getMessageType(),
//                new String(message.getEncryptedContent()),
//                message.getContentHash(),
//                message.getReplyTo() != null ? message.getReplyTo().getMessageId() : null,
//                message.getCreatedAt(),
//                message.getMediaAttachments().stream()
//                        .map(this::convertMediaToDto)
//                        .collect(Collectors.toList())
//        );
//    }
//
//    private MediaDto convertMediaToDto(Media media) {
//        return new MediaDto(
//                media.getId(),
//                media.getFileType(),
//                media.getFileSize(),
//                media.getDurationSec(),
//                media.getThumbnailUrl()
//        );
//    }
//}

package com.nova.poneglyph.service.chat;

import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.conversation.Participant;
import com.nova.poneglyph.domain.enums.DeliveryStatus;
import com.nova.poneglyph.domain.message.*;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.dto.chatDto.MediaAttachmentDto;
import com.nova.poneglyph.dto.chatDto.MediaDto;
import com.nova.poneglyph.dto.chatDto.MessageDto;
import com.nova.poneglyph.dto.chatDto.MessageSendDto;
import com.nova.poneglyph.exception.*;
import com.nova.poneglyph.repository.*;
import com.nova.poneglyph.service.WebSocketService;
import com.nova.poneglyph.service.media.MediaService;
import com.nova.poneglyph.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageStatusRepository statusRepository;
    private final MessageReactionRepository reactionRepository;
    private final ParticipantRepository participantRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final WebSocketService webSocketService;
    private final MediaService mediaService;

    @Transactional
    public MessageDto sendMessage(UUID conversationId, UUID senderId, MessageSendDto dto) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new MessageException("Conversation not found"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new MessageException("User not found"));

        String content = dto.getContent() == null ? "" : dto.getContent();
        String encryptedContent = dto.isEncrypt()
                ? encryptionUtil.encrypt(content, conversation.getEncryptionKey())
                : content;

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .messageType(dto.getMessageType())
                .encryptedContent(encryptedContent.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .contentHash(encryptionUtil.hash(content))
                .replyTo(dto.getReplyToId() != null
                        ? messageRepository.findById(dto.getReplyToId()).orElse(null)
                        : null)
                .build();

        message = messageRepository.save(message);

        // مرفقات الميديا
        if (dto.getMediaAttachments() != null && !dto.getMediaAttachments().isEmpty()) {
            for (MediaAttachmentDto m : dto.getMediaAttachments()) {
                mediaService.attachMediaToMessage(message, m);
            }
        }

        // تحديث آخر وقت
        conversation.setLastMessageAt(OffsetDateTime.now());
        conversationRepository.save(conversation);

        // حالات التسليم الأولية
        createInitialStatuses(message, conversation);

        // إشعار عبر WebSocket
        MessageDto out = convertToDto(message);
        webSocketService.notifyNewMessage(conversationId, out);
        return out;
    }

    @Transactional
    public void deleteMessage(UUID messageId, UUID userId, boolean forEveryone) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message not found"));

        boolean isOwner = message.getSender() != null && message.getSender().getId().equals(userId);
        if (!isOwner) {
            Participant requester = participantRepository
                    .findByConversation_IdAndUser_Id(message.getConversation().getId(), userId)
                    .orElseThrow(() -> new PermissionDeniedException("Not in conversation"));
            if (!requester.getRole().isAdmin()) {
                throw new PermissionDeniedException("Insufficient privileges");
            }
        }

        if (forEveryone) {
            message.setDeletedForAll(true);
            message.setDeletedAt(OffsetDateTime.now());
            messageRepository.save(message);
        } else {
            MessageStatus status = statusRepository
                    .findByMessage_IdAndUser_Id(messageId, userId)
                    .orElseThrow(() -> new MessageException("Status not found"));
            // نفترض إضافة حقل منطقي في MessageStatus اسمه deleted
            status.setStatus(DeliveryStatus.DELETED); // أو status.setDeleted(true) إن وجد
            status.setUpdatedAt(OffsetDateTime.now());
            statusRepository.save(status);
        }

        webSocketService.notifyMessageDeleted(messageId, forEveryone);
    }

    @Transactional
    public void addReaction(UUID messageId, UUID userId, String reaction) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MessageException("User not found"));

        MessageReaction.PK pk = new MessageReaction.PK(messageId, userId);
        MessageReaction entity = reactionRepository.findByMessage_IdAndUser_Id(messageId, userId)
                .orElseGet(() -> {
                    MessageReaction r = new MessageReaction();
                    r.setMessage(message);
                    r.setUser(user);
                    return r;
                });

        entity.setReaction(reaction);
        entity.setCreatedAt(OffsetDateTime.now());
        reactionRepository.save(entity);

        webSocketService.notifyReactionUpdate(messageId, userId, reaction);
    }

    private void createInitialStatuses(Message message, Conversation conversation) {
        List<Participant> participants = participantRepository.findByConversation(conversation);
        for (Participant p : participants) {
            DeliveryStatus st = p.getUser().getId().equals(message.getSender().getId())
                    ? DeliveryStatus.SENT : DeliveryStatus.SENT; // لاحقًا غيّرها للمرسل "SENT" ولغيره "DELIVERED" عند التسليم الفعلي
            MessageStatus ms = new MessageStatus();
            ms.setMessage(message);
            ms.setUser(p.getUser());
            ms.setStatus(st);
            ms.setUpdatedAt(OffsetDateTime.now());
            statusRepository.save(ms);
        }
    }

    private MessageDto convertToDto(Message message) {
        java.util.List<MediaDto> medias = message.getMediaAttachments() == null ? java.util.List.of()
                : message.getMediaAttachments().stream()
                .map(this::convertMediaToDto).toList();

        return new MessageDto(
                message.getId(),
                message.getConversation().getId(),
                message.getSender() != null ? message.getSender().getId() : null,
                message.getMessageType(),
                new String(message.getEncryptedContent(), java.nio.charset.StandardCharsets.UTF_8),
                message.getContentHash(),
                message.getReplyTo() != null ? message.getReplyTo().getId() : null,
                message.getCreatedAt(), // من Auditable
                medias
        );
    }

    private MediaDto convertMediaToDto(Media media) {
        return new MediaDto(
                media.getId(),
                media.getFileType(),
                media.getFileSize(),
                media.getDurationSec(),
                media.getThumbnailUrl()
        );
    }
}
