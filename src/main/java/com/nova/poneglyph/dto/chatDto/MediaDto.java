package com.nova.poneglyph.dto.chatDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MediaDto {
    private Long mediaId;
    private String fileType;
    private Long fileSize;
    private Integer durationSec;
    private String thumbnailUrl;
}
