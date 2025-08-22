package com.nova.poneglyph.service.call;



import com.nova.poneglyph.domain.conversation.Conversation;
import com.nova.poneglyph.domain.message.Call;
import com.nova.poneglyph.domain.user.User;

import com.nova.poneglyph.dto.callDto.CallDto;
import com.nova.poneglyph.dto.callDto.CallInitiateDto;
import com.nova.poneglyph.exception.CallException;
import com.nova.poneglyph.repository.CallRepository;
import com.nova.poneglyph.repository.ConversationRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.util.EncryptionUtil;
import com.nova.poneglyph.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CallService {

    private final CallRepository callRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final WebSocketService webSocketService;

    @Transactional
    public CallDto initiateCall(CallInitiateDto dto, UUID callerId) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new CallException("Caller not found"));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new CallException("Receiver not found"));

        Conversation conversation = dto.getConversationId() != null ?
                conversationRepository.findById(dto.getConversationId())
                        .orElseThrow(() -> new CallException("Conversation not found")) :
                null;

        String encryptionKey = EncryptionUtil.generateKey();

        Call call = new Call();
        call.setCaller(caller);
        call.setReceiver(receiver);
        call.setConversation(conversation);
        call.setCallType(dto.getCallType());
        call.setStatus("initiated");
        call.setEncryptionKey(encryptionKey);
        call.setRecorded(dto.isRecorded());
        call = callRepository.save(call);

        // Notify receiver via WebSocket
        webSocketService.notifyIncomingCall(call);

        return convertToDto(call);
    }

    @Transactional
    public void updateCallStatus(UUID callId, String status, UUID userId) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new CallException("Call not found"));

        // Validate user is part of the call
        if (!call.getCaller().getId().equals(userId) &&
                !call.getReceiver().getId().equals(userId)) {
            throw new CallException("User not part of this call");
        }

        call.setStatus(status);

        if ("ongoing".equals(status)) {
            call.setStartTime(OffsetDateTime.now());
        } else if ("completed".equals(status) || "missed".equals(status) || "rejected".equals(status)) {
            call.setEndTime(OffsetDateTime.now());
            if (call.getStartTime() != null) {
                call.setDurationSec((int) (OffsetDateTime.now().toEpochSecond() - call.getStartTime().toEpochSecond()));
            }
        }

        callRepository.save(call);
        webSocketService.notifyCallStatus(callId, status);
    }

    @Transactional(readOnly = true)
    public CallDto getCall(UUID callId, UUID userId) {
        Call call = callRepository.findById(callId)
                .orElseThrow(() -> new CallException("Call not found"));

        // Validate user is part of the call
        if (!call.getCaller().getId().equals(userId) &&
                !call.getReceiver().getId().equals(userId)) {
            throw new CallException("User not part of this call");
        }

        return convertToDto(call);
    }

    private CallDto convertToDto(Call call) {
        return new CallDto(
                call.getId(),
                call.getCaller().getId(),
                call.getReceiver().getId(),
                call.getConversation() != null ? call.getConversation().getId() : null,
                call.getCallType(),
                call.getStatus(),
                call.getStartTime(),
                call.getEndTime(),
                call.getDurationSec(),
                call.getEncryptionKey(),
                call.isRecorded()
        );
    }
}
