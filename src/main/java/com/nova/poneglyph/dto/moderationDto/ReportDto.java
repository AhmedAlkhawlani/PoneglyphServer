package com.nova.poneglyph.dto.moderationDto;


import lombok.Data;
import java.util.UUID;

@Data
public class ReportDto {
    private UUID reportedUserId;
    private String reportedPhone;
    private String reportType;
    private String reportDetails;
}

