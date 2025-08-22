////package com.nova.poneglyph.dto;
////
////
////
////import jakarta.validation.constraints.NotNull;
////
////
////import java.util.UUID;
//
//public class CallDto1 {
//
//    public record CallInitiateDto(
//            UUID receiverId,
//            UUID conversationId,
//            @NotNull String callType,
//            boolean recorded
//    ) {}
//
//    public record CallDto(
//            UUID callId,
//            UUID callerId,
//            UUID receiverId,
//            UUID conversationId,
//            String callType,
//            String status,
//            java.time.OffsetDateTime startTime,
//            java.time.OffsetDateTime endTime,
//            Integer durationSec,
//            String encryptionKey,
//            boolean recorded
//    ) {}
//}
////
//package com.nova.gen3.dto;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Pattern;
//import lombok.Data;
//
