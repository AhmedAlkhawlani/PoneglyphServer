//package com.nova.poneglyph.api.controller.chat;
//
//import com.nova.poneglyph.dto.MessageDTO;
//import com.nova.poneglyph.dto.SendMessageRequest;
//import com.nova.poneglyph.service.MessageService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/messages")
//@RequiredArgsConstructor
//public class MessageController {
//    private final MessageService messageService;
//
//    @PostMapping("/send")
//    public ResponseEntity<MessageDTO> sendMessage(
//            @RequestBody @Valid SendMessageRequest request) {
//        return ResponseEntity.ok(messageService.sendMessage(request));
//    }
//
//    @PostMapping("/{messageId}/markAsDelivered")
//    public ResponseEntity<Void> markAsDelivered(@PathVariable String messageId) {
//        messageService.markAsDelivered(messageId);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/{messageId}/markAsSeen")
//    public ResponseEntity<Void> markAsSeen(@PathVariable String messageId) {
//        messageService.markAsSeen(messageId);
//        return ResponseEntity.ok().build();
//    }
//}
