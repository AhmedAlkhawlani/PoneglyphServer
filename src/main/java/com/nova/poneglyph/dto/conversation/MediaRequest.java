package com.nova.poneglyph.dto.conversation;

import lombok.Data;

@Data
public class MediaRequest {
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String thumbnailUrl;
    private Integer durationSec;
}
