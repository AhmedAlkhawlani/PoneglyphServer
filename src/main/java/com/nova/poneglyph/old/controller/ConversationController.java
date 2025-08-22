//package com.nova.poneglyph.api.controller;
//
//import com.nova.poneglyph.dto.ConversationDTO;
//import com.nova.poneglyph.dto.MessageDTO;
//import com.nova.poneglyph.dto.StartConversationRequest;
//import com.nova.poneglyph.service.ConversationService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/conversations")
//@RequiredArgsConstructor
//public class ConversationController {
//    private final ConversationService conversationService;
//
//    @GetMapping("/{phoneNumber}")
//    public ResponseEntity<List<ConversationDTO>> getUserConversations(
//            @PathVariable String phoneNumber,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size) {
//        return ResponseEntity.ok(conversationService.getUserConversations(phoneNumber, page, size));
//    }
//
//    @PostMapping("/start")
//    public ResponseEntity<ConversationDTO> startConversation(
//            @RequestBody @Valid StartConversationRequest request) {
//        return ResponseEntity.ok(conversationService.startConversation(request));
//    }
//
//    @GetMapping("/{conversationId}/messages")
//    public ResponseEntity<List<MessageDTO>> getConversationMessages(
//            @PathVariable String conversationId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "50") int size) {
//        return ResponseEntity.ok(conversationService.getConversationMessages(conversationId, page, size));
//    }
//}
