package com.nova.poneglyph.domain.message;

import com.nova.poneglyph.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "media",
        indexes = {
                @Index(name = "idx_media_message", columnList = "message_id"),
                @Index(name = "idx_media_uploader", columnList = "uploader_id")
        })
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "encryption_key")
    private String encryptionKey;
}
