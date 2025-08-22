package com.nova.poneglyph.dto.chatDto;

public class MediaAttachmentDto {
    private String fileUrl;        // رابط الملف بعد الحفظ
    private String thumbnailUrl;   // رابط الصورة المصغرة (للفيديو/الصورة)
    private String encryptionKey;  // مفتاح التشفير (لو عندك تشفير)
    private String fileType;
    private String description;
    private Integer durationSec;
    private Long fileSize;

    // Getters & Setters
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getEncryptionKey() { return encryptionKey; }
    public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}

