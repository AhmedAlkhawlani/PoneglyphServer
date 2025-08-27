//package com.nova.poneglyph.api.controller.chat;
//
//
//
//
//import com.nova.poneglyph.dto.chatDto.ConversationCreateDto;
//import com.nova.poneglyph.dto.chatDto.ConversationDto;
//import com.nova.poneglyph.dto.chatDto.MessageDto;
//import com.nova.poneglyph.dto.chatDto.MessageSendDto;
//import com.nova.poneglyph.service.chat.ConversationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/conversations")
//@RequiredArgsConstructor
//public class ConversationController {
//
//    private final ConversationService conversationService;
//
//    @PostMapping
//    public ResponseEntity<ConversationDto> createConversation(
//            @RequestBody ConversationCreateDto dto,
//            @AuthenticationPrincipal UUID userId) {
//        return ResponseEntity.ok(conversationService.createConversation(dto, userId));
//    }
//
//    @GetMapping("/{conversationId}")
//    public ResponseEntity<ConversationDetailDto> getConversation(
//            @PathVariable UUID conversationId,
//            @AuthenticationPrincipal UUID userId) {
//        return ResponseEntity.ok(conversationService.getConversationDetails(conversationId, userId));
//    }
//
//    @PostMapping("/{conversationId}/participants")
//    public ResponseEntity<Void> addParticipants(
//            @PathVariable UUID conversationId,
//            @RequestBody Set<UUID> userIds,
//            @AuthenticationPrincipal UUID requesterId) {
//        conversationService.addParticipants(conversationId, userIds, requesterId);
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/{conversationId}/participants/{userId}")
//    public ResponseEntity<Void> removeParticipant(
//            @PathVariable UUID conversationId,
//            @PathVariable UUID userId,
//            @AuthenticationPrincipal UUID requesterId) {
//        conversationService.removeParticipant(conversationId, userId, requesterId);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/{conversationId}/messages")
//    public ResponseEntity<MessageDto> sendMessage(
//            @PathVariable UUID conversationId,
//            @RequestBody MessageSendDto dto,
//            @AuthenticationPrincipal UUID senderId) {
//        return ResponseEntity.ok(conversationService.sendMessage(conversationId, dto, senderId));
//    }
//
//    @GetMapping("/{conversationId}/messages")
//    public ResponseEntity<List<MessageDto>> getMessages(
//            @PathVariable UUID conversationId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "50") int size,
//            @AuthenticationPrincipal UUID userId) {
//        return ResponseEntity.ok(conversationService.getMessages(conversationId, page, size, userId));
//    }
//
//    @PutMapping("/{conversationId}/mute")
//    public ResponseEntity<Void> muteConversation(
//            @PathVariable UUID conversationId,
//            @RequestParam(required = false) Long hours,
//            @AuthenticationPrincipal UUID userId) {
//        conversationService.muteConversation(conversationId, userId, hours);
//        return ResponseEntity.ok().build();
//    }
//}

// ConversationController.java
package com.nova.poneglyph.api.controller.chat;

import com.nova.poneglyph.config.v2.CustomUserDetails;
import com.nova.poneglyph.dto.conversation.ConversationDTO;
import com.nova.poneglyph.dto.conversation.CreateConversationRequest;
import com.nova.poneglyph.dto.conversation.MessageDTO;
import com.nova.poneglyph.dto.conversation.SendMessageRequest;

import com.nova.poneglyph.service.chat.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getUserConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = userDetails.getId();
        List<ConversationDTO> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateConversationRequest request) {
        UUID userId = userDetails.getId();
        ConversationDTO conversation = conversationService.createConversation(userId, request);
        return ResponseEntity.ok(conversation);
    }

//    @GetMapping("/{conversationId}")
//    public ResponseEntity<ConversationDTO> getConversation(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @PathVariable UUID conversationId) {
//        UUID userId = userDetails.getId();
//        ConversationDTO conversation = conversationService.getConversation(userId, conversationId);
//        return ResponseEntity.ok(conversation);
//    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID userId = userDetails.getId();
        List<MessageDTO> messages = conversationService.getMessages(userId, conversationId, page, size);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageDTO> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        UUID userId = userDetails.getId();
        MessageDTO message = conversationService.sendMessage(userId, conversationId, request);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId) {
        UUID userId = userDetails.getId();
        conversationService.markAsRead(userId, conversationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId) {
        UUID userId = userDetails.getId();
        conversationService.deleteConversation(userId, conversationId);
        return ResponseEntity.ok().build();
    }
}
