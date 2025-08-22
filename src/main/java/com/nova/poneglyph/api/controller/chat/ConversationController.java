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
