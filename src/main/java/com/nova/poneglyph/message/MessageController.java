//package com.nova.poneglyph.message;
//
////import io.swagger.v3.oas.annotations.Parameter;
////import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//@RestController
//@RequestMapping("/api/v1/messages")
//@RequiredArgsConstructor
//public class MessageController {
//
//    private final MessageService messageService;
//
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public MessageResponse saveMessage(@RequestBody MessageRequest message) {
//        return messageService.saveMessage(message);
//    }
//
//    @PostMapping(value = "/upload-media", consumes = "multipart/form-data")
//    @ResponseStatus(HttpStatus.CREATED)
//    public void uploadMedia(
//            @RequestParam("chat-id") String chatId,
//            @RequestPart("file") MultipartFile file,
//            Authentication authentication
//    ) {
//        messageService.uploadMediaMessage(chatId, file, authentication);
//    }
//
//    // ✅ تحديث الرسائل إلى SEEN
//    @PatchMapping
//    @ResponseStatus(HttpStatus.ACCEPTED)
//    public void setMessageToSeen(
//            @RequestParam("chat-id") String chatId,
//            @RequestParam("receiver-id") String receiverId,
//            Authentication authentication
//    ) {
//        messageService.setMessagesToSeen(chatId, receiverId, authentication);
//    }
//
//    @GetMapping("/chat/{chat-id}")
//    public ResponseEntity<List<MessageResponse>> getAllMessages(
//            @PathVariable("chat-id") String chatId
//    ) {
//        return ResponseEntity.ok(messageService.findChatMessages(chatId));
//    }
//
//    // ✅ حالة SENDING (قبل الحفظ)
//    @PatchMapping("/{id}/sending")
//    @ResponseStatus(HttpStatus.ACCEPTED)
//    public void markAsSending(@PathVariable String id) {
//        messageService.updateMessageState(id, MessageState.SENDING);
//    }
//    // ✅ حالة SENDING (قبل الحفظ)
//    @PatchMapping("/{id}/seen")
//    @ResponseStatus(HttpStatus.ACCEPTED)
//    public void markAsSeen(@PathVariable String id) {
//        messageService.updateMessageState(id, MessageState.SENDING);
//    }
//
//    // ✅ حالة SENT (تم الإرسال)
//    @PatchMapping("/{id}/sent")
//    @ResponseStatus(HttpStatus.ACCEPTED)
//    public void markAsSent(@PathVariable String id) {
//        messageService.updateMessageState(id, MessageState.SENT);
//    }
//
//    // ✅ حالة DELIVERED (وصلت للمستقبل)
//    @PatchMapping("/{id}/delivered")
//    @ResponseStatus(HttpStatus.ACCEPTED)
//    public void markAsDelivered(@PathVariable String id) {
//        messageService.updateMessageState(id, MessageState.DELIVERED);
//    }
//}
