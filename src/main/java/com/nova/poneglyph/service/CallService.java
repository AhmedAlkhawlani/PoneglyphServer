//package com.nova.poneglyph.service;
//
//
//
//import com.nova.poneglyph.domain.message.Call;
//import com.nova.poneglyph.domain.conversation.Conversation;
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.dto.CallInitiateDto;
//import com.nova.poneglyph.exception.CallException;
//import com.nova.poneglyph.repository.CallRepository;
//import com.nova.poneglyph.repository.ConversationRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.util.EncryptionUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.OffsetDateTime;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class CallService {
//
//    private final CallRepository callRepository;
//    private final UserRepository userRepository;
//    private final ConversationRepository conversationRepository;
//    private final WebSocketService webSocketService;
//
//    @Transactional
//    public Call initiateCall(CallInitiateDto dto, UUID callerId) {
//        User caller = userRepository.findById(callerId)
//                .orElseThrow(() -> new CallException("Caller not found"));
//
//        User receiver = userRepository.findById(dto.getReceiverId())
//                .orElseThrow(() -> new CallException("Receiver not found"));
//
//        Conversation conversation = dto.getConversationId() != null ?
//                conversationRepository.findById(dto.getConversationId())
//                        .orElseThrow(() -> new CallException("Conversation not found")) :
//                null;
//
//        String encryptionKey = EncryptionUtil.generateKey();
//
//        Call call = Call.builder()
//                .caller(caller)
//                .receiver(receiver)
//                .conversation(conversation)
//                .callType(dto.getCallType())
//                .status("initiated")
//                .encryptionKey(encryptionKey)
//                .recorded(dto.isRecorded())
//                .build();
//
//        call = callRepository.save(call);
//
//        // Notify receiver via WebSocket
//        webSocketService.notifyIncomingCall(call);
//
//        return call;
//    }
//
//    @Transactional
//    public void updateCallStatus(UUID callId, String status) {
//        callRepository.findById(callId).ifPresent(call -> {
//            call.setStatus(status);
//            if ("ongoing".equals(status)) {
//                call.setStartTime(OffsetDateTime.now());
//            } else if ("completed".equals(status)) {
//                call.setEndTime(OffsetDateTime.now());
//                if (call.getStartTime() != null) {
//                    call.setDurationSec((int) (OffsetDateTime.now().toEpochSecond() - call.getStartTime().toEpochSecond()));
//                }
//            }
//            callRepository.save(call);
//        });
//    }
//}
