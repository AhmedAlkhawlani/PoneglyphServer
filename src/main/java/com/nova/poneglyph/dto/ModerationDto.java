//package com.nova.poneglyph.dto;
//
//
//
//import com.nova.poneglyph.domain.enums.ReportStatus;
//import java.util.UUID;
//
//public class ModerationDto {
//
//    public record ReportDto(
//            UUID reportedUserId,
//            String reportedPhone,
//            String reportType,
//            String reportDetails
//    ) {}
//
//    public record ReportResponseDto(
//            Long reportId,
//            UUID reporterId,
//            UUID reportedUserId,
//            String reportType,
//            String reportDetails,
//            ReportStatus status,
//            java.time.OffsetDateTime createdAt
//    ) {}
//}
