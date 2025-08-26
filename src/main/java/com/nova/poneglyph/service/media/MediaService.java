package com.nova.poneglyph.service.media;



import com.nova.poneglyph.domain.message.Media;
import com.nova.poneglyph.domain.message.Message;
import com.nova.poneglyph.domain.user.User;

import com.nova.poneglyph.dto.MediaUploadDto;
import com.nova.poneglyph.dto.chatDto.MediaAttachmentDto;
import com.nova.poneglyph.exception.MediaException;

import com.nova.poneglyph.repository.MediaRepository;
import com.nova.poneglyph.repository.MessageRepository;
import com.nova.poneglyph.repository.ParticipantRepository;
import com.nova.poneglyph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MediaService {

    @Value("${media.upload-dir}")
    private String uploadDir;
    private final MediaRepository mediaRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final FileStorageService fileStorageService;
    @Transactional
    public String uploadMedia(MediaUploadDto dto, MultipartFile file, UUID uploaderId) {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new MediaException("Uploader not found"));
        Message message = null;
        if (dto.getMessageId() != null) {
            UUID messageId = UUID.fromString(dto.getMessageId());
            message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new MediaException("Message not found"));
        }
        try {
            // استخدام FileStorageService لرفع الملف
            String fileId = fileStorageService.storeFile(file, uploaderId);
            // ✅ بناء كائن Media
            Media media = new Media();
            media.setMessage(message);
            media.setUploader(uploader);
            media.setFileUrl(fileId); // تخزين معرف الملف بدلاً من المسار
            media.setFileType(file.getContentType());
            media.setFileSize(file.getSize());
            media.setDurationSec(dto.getDurationSec());
            media.setThumbnailUrl(generateThumbnail(file, uploaderId));
            media.setEncryptionKey(null); // تقدر تطبق تشفير هنا لاحقًا
            mediaRepository.save(media);
            return fileId;
        } catch (Exception e) {
            throw new MediaException("Failed to upload media: " + e.getMessage());
        }
    }
    private String generateThumbnail(MultipartFile file, UUID uploaderId) {
        // TODO: إنشاء ثامبنييل باستخدام FileStorageService
        // تقدر تستخدم FFmpeg أو Thumbnailator هنا
        return null;
    }
    @Transactional(readOnly = true)
    public Media getMedia(Long mediaId, UUID userId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new MediaException("Media not found"));
        if (media.getMessage() != null) {
            boolean allowed = participantRepository.existsByConversationAndUser(
                    media.getMessage().getConversation(),
                    userRepository.findById(userId).orElseThrow()
            );
            if (!allowed) throw new MediaException("Access denied");
        }
        return media;
    }




    @Transactional
    public void attachMediaToMessage(Message message, MediaAttachmentDto dto) {
        Media media = new Media();
        media.setMessage(message);
        media.setUploader(message.getSender());
        media.setFileUrl(dto.getFileUrl());
        media.setFileType(dto.getFileType());
        // لو أضفت fileSize و durationSec في DTO، فك التعليق:
         media.setFileSize(dto.getFileSize());
         media.setDurationSec(dto.getDurationSec());
        media.setThumbnailUrl(dto.getThumbnailUrl());
        media.setEncryptionKey(dto.getEncryptionKey());

        mediaRepository.save(media);
    }



}
