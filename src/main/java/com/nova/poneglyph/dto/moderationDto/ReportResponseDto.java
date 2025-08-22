package com.nova.poneglyph.dto.moderationDto;

import com.nova.poneglyph.domain.enums.ReportStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class ReportResponseDto {
    private Long reportId;
    private UUID reporterId;
    private UUID reportedUserId;
    private String reportType;
    private String reportDetails;
    private ReportStatus status;
    private java.time.OffsetDateTime createdAt;
}
