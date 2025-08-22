//package com.nova.poneglyph.dto;
//
//
//
//import com.nova.poneglyph.domain.enums.ConversationType;
//import com.nova.poneglyph.domain.enums.MessageType;
//import jakarta.validation.constraints.NotNull;
//import java.util.Set;
//import java.util.UUID;
//
//public class ChatDto {
//
//    public record ConversationCreateDto(
//            @NotNull ConversationType type,
//            String title,
//            Set<UUID> participantIds,
//            boolean isGroup
//    ) {}
//
//    public record ConversationDto(
//            UUID conversationId,
//            ConversationType type,
//            String encryptionKey,
//            java.time.OffsetDateTime lastMessageAt
//    ) {}
//
//    public record MessageSendDto(
//            @NotNull UUID conversationId,
//            @NotNull String content,
//            @NotNull MessageType messageType,
//            boolean encrypt,
//            UUID replyToId,
//            Set<MediaAttachmentDto> mediaAttachments
//    ) {}
//
//    public record MessageDto(
//            UUID messageId,
//            UUID conversationId,
//            UUID senderId,
//            MessageType messageType,
//            String content,
//            String contentHash,
//            UUID replyToId,
//            java.time.OffsetDateTime sentAt,
//            Set<MediaDto> mediaAttachments
//    ) {}
//
//    public record MediaAttachmentDto(
//            String fileType,
//            long fileSize,
//            Integer durationSec
//    ) {}
//
//    public record MediaDto(
//            Long mediaId,
//            String fileType,
//            Long fileSize,
//            Integer durationSec,
//            String thumbnailUrl
//    ) {}
//}
