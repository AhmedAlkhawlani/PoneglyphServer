package com.nova.poneglyph.dto;



import org.springframework.web.multipart.MultipartFile;

public class MediaUploadDto {

    private String type;        // image, video, audio
    private String description;
    private String messageId;   // لربط الملف بالرسالة
    private Integer durationSec;   // للفيديو أو الصوت


    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
}

