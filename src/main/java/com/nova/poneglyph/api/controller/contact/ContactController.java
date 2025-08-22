//package com.nova.poneglyph.api.controller.contact;
//
//
//
//import com.nova.poneglyph.dto.contactDto.ContactDto;
//import com.nova.poneglyph.dto.contactDto.ContactSyncDto;
//import com.nova.poneglyph.service.contact.ContactService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/contacts")
//@RequiredArgsConstructor
//public class ContactController {
//
//    private final ContactService contactService;
//
//    @PostMapping("/sync")
//    public ResponseEntity<Void> syncContacts(
//            @RequestBody ContactSyncDto syncDto,
//            @AuthenticationPrincipal UUID userId) {
//        contactService.syncContacts(userId, syncDto);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping
//    public ResponseEntity<List<ContactDto>> getContacts(
//            @AuthenticationPrincipal UUID userId) {
//        return ResponseEntity.ok(contactService.getContacts(userId));
//    }
//
//    @PostMapping("/block/{phone}")
//    public ResponseEntity<Void> blockContact(
//            @PathVariable String phone,
//            @AuthenticationPrincipal UUID userId) {
//        contactService.blockContact(userId, phone);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/unblock/{phone}")
//    public ResponseEntity<Void> unblockContact(
//            @PathVariable String phone,
//            @AuthenticationPrincipal UUID userId) {
//        contactService.unblockContact(userId, phone);
//        return ResponseEntity.ok().build();
//    }
//}
