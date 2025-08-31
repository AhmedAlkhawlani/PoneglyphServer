//package com.nova.poneglyph.api.controller.chat;
//
//import com.nova.poneglyph.config.v2.CustomUserDetails;
//import com.nova.poneglyph.dto.conversation.ConversationDTO;
//import com.nova.poneglyph.dto.conversation.CreateConversationRequest;
//import com.nova.poneglyph.dto.conversation.MessageDTO;
//import com.nova.poneglyph.dto.conversation.SendMessageRequest;
//
//import com.nova.poneglyph.service.WebSocketService;
//import com.nova.poneglyph.service.chat.ConversationService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/conversations")
//@RequiredArgsConstructor
//public class ConversationController {
//
//    private final ConversationService conversationService;
//    private final WebSocketService socketService;
//
//    @GetMapping
//    public ResponseEntity<List<ConversationDTO>> getUserConversations(
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//        UUID userId = userDetails.getId();
//        List<ConversationDTO> conversations = conversationService.getUserConversations(userId);
//        return ResponseEntity.ok(conversations);
//    }
//
//    @PostMapping
//    public ResponseEntity<ConversationDTO> createConversation(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @Valid @RequestBody CreateConversationRequest request) {
//        UUID userId = userDetails.getId();
//        ConversationDTO conversation = conversationService.createConversation(userId, request);
//        return ResponseEntity.ok(conversation);
//    }
//
//    @GetMapping("/{conversationId}")
//    public ResponseEntity<ConversationDTO> getConversation(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @PathVariable UUID conversationId) {
//        UUID userId = userDetails.getId();
//        ConversationDTO conversation = conversationService.getConversation(userId, conversationId);
//        return ResponseEntity.ok(conversation);
//    }
//
//    @GetMapping("/{conversationId}/messages")
//    public ResponseEntity<List<MessageDTO>> getMessages(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @PathVariable UUID conversationId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "50") int size) {
//        UUID userId = userDetails.getId();
//        List<MessageDTO> messages = conversationService.getMessages(userId, conversationId, page, size);
//        return ResponseEntity.ok(messages);
//    }
//
//    @PostMapping("/{conversationId}/messages")
//    public ResponseEntity<MessageDTO> sendMessage(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @PathVariable UUID conversationId,
//            @Valid @RequestBody SendMessageRequest request) {
//        UUID userId = userDetails.getId();
//        MessageDTO message = conversationService.sendMessage(userId, conversationId, request);
////        socketService.notifyNewMessage(conversationId,message);
//        return ResponseEntity.ok(message);
//    }
//
//    @PutMapping("/{conversationId}/read")
//    public ResponseEntity<Void> markAsRead(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @PathVariable UUID conversationId) {
//        UUID userId = userDetails.getId();
//        conversationService.markAsRead(userId, conversationId);
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/{conversationId}")
//    public ResponseEntity<Void> deleteConversation(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @PathVariable UUID conversationId) {
//        UUID userId = userDetails.getId();
//        conversationService.deleteConversation(userId, conversationId);
//        return ResponseEntity.ok().build();
//    }
//}

package com.nova.poneglyph.api.controller.chat;

import com.nova.poneglyph.config.v2.CustomUserDetails;
import com.nova.poneglyph.dto.conversation.ConversationDTO;
import com.nova.poneglyph.dto.conversation.CreateConversationRequest;
import com.nova.poneglyph.dto.conversation.MessageDTO;
import com.nova.poneglyph.dto.conversation.SendMessageRequest;
import com.nova.poneglyph.service.WebSocketService;
import com.nova.poneglyph.service.chat.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ConversationController — واجهات REST لإدارة المحادثات (قائمة المحادثات، إنشاء، رسائل، مشاركون...)
 *
 * التغييرات الأساسية:
 * - تمرير currentUserId إلى Service في كل نداء.
 * - استخدام notify عبر WebSocketService بعد إنشاء محادثة أو إرسال رسالة.
 * - إضافة نقاط نهاية لإضافة/إزالة مشاركين وتحديث عنوان المحادثة.
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final WebSocketService socketService;

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

        // إخطار عبر WebSocket للمشتركين (إن رغبت)
        try {
            socketService.notifyConversationCreated(conversation);
        } catch (Exception ignored) {
            // لا تفشل العملية الرئيسية بسبب فشل إخطار الويب سوكيت
        }

        // نعيد 201 CREATED مع DTO (لو أحببت تعيد موقع المورد: URI)
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> getConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId) {

        UUID userId = userDetails.getId();
        ConversationDTO conversation = conversationService.getConversation(userId, conversationId);
        return ResponseEntity.ok(conversation);
    }

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

        // أخطر المشتركين عبر WebSocket عن الرسالة الجديدة
        try {
            socketService.notifyNewMessage(conversationId, message);
        } catch (Exception ignored) {
            // لا تفشل الطلب بسبب إخطار WebSocket
        }

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

    /**
     * حذف أو مغادرة المحادثة: سيتم حذف المحادثة إذا كان المستخدم ADMIN (مالك)،
     * وإلا سيغادر المستخدم فقط.
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId) {

        UUID userId = userDetails.getId();
        // استدعاء الخدمة التي تتعامل مع الحذف أو المغادرة وفق الدور
        conversationService.deleteOrLeaveConversation(userId, conversationId);
        return ResponseEntity.ok().build();
    }

    // ======================
    // إدارة المشاركين
    // ======================

    /**
     * إضافة مشاركين جدد للمحادثة.
     * Body: قائمة UUIDs للأعضاء الجدد (مثال JSON: ["uuid1","uuid2"])
     */
    @PostMapping("/{conversationId}/participants")
    public ResponseEntity<Void> addParticipants(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId,
            @RequestBody List<UUID> participantIds) {

        UUID userId = userDetails.getId();
        Set<UUID> ids = new HashSet<>(participantIds != null ? participantIds : List.of());
        conversationService.addParticipants(conversationId, ids, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * إزالة مشارك من المحادثة (أو مغادرة المشترك نفسه).
     */
    @DeleteMapping("/{conversationId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId,
            @PathVariable UUID participantId) {

        UUID userId = userDetails.getId();
        // نمرّر requesterId = userId للتحقق من الصلاحيات داخل الخدمة
        conversationService.removeParticipant(conversationId, participantId, userId);
        return ResponseEntity.ok().build();
    }

    // ======================
    // تحديث عنوان المجموعة (اختياري — تأكد من وجود الميثود في Service)
    // ======================
    public static class TitleUpdateRequest {
        public String title;
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    @PutMapping("/{conversationId}/title")
    public ResponseEntity<Void> updateConversationTitle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID conversationId,
            @Valid @RequestBody TitleUpdateRequest body) {

        UUID userId = userDetails.getId();
        String title = body != null ? body.getTitle() : null;
        if (title == null) return ResponseEntity.badRequest().build();

        // يتطلب وجود method updateConversationTitle في ConversationService
        conversationService.updateConversationTitle(userId, conversationId, title);
        return ResponseEntity.ok().build();
    }
}
