//package com.nova.poneglyph.api.controller;
//
//
//
//import com.nova.poneglyph.domain.conversation.Conversation;
//import com.nova.poneglyph.domain.message.Message;
//import com.nova.poneglyph.dto.*;
//import com.nova.poneglyph.service.ChatService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Set;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/chat")
//@RequiredArgsConstructor
//public class ChatController {
//
//    private final ChatService chatService;
//
//    @PostMapping("/conversations")
//    public ResponseEntity<Conversation> createConversation(
//            @RequestBody ConversationCreateDto dto,
//            @AuthenticationPrincipal UUID userId) {
//        return ResponseEntity.ok(chatService.createConversation(dto, userId));
//    }
//
//    @PostMapping("/conversations/{conversationId}/participants")
//    public ResponseEntity<Void> addParticipants(
//            @PathVariable UUID conversationId,
//            @RequestBody Set<UUID> userIds,
//            @AuthenticationPrincipal UUID requesterId) {
//        chatService.addParticipants(conversationId, userIds, requesterId);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/messages")
//    public ResponseEntity<Void> sendMessage(
//            @RequestBody MessageSendDto dto,
//            @AuthenticationPrincipal UUID senderId) {
//        chatService.sendMessage(dto.getConversationId(), senderId, dto);
//        return ResponseEntity.ok().build();
//    }
//}
