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

//@Service
//@RequiredArgsConstructor
//public class MediaService {
//
//    @Value("${media.upload.dir}")
//    private String uploadDir;
//
//    private final MediaRepository mediaRepository;
//    private final MessageRepository messageRepository;
//    private final UserRepository userRepository;
//
//    @Transactional
//    public String uploadMedia(MediaUploadDto dto, MultipartFile file, UUID uploaderId) {
//        User uploader = userRepository.findById(uploaderId)
//                .orElseThrow(() -> new MediaException("Uploader not found"));
//
//        Message message = null;
//        if (dto.getMessageId() != null) {
//            message = messageRepository.findById(dto.getMessageId())
//                    .orElseThrow(() -> new MediaException("Message not found"));
//        }
//
//        try {
//            // Create upload directory if not exists
//            Path uploadPath = Paths.get(uploadDir);
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//
//            // Generate unique filename
//            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            Path filePath = uploadPath.resolve(filename);
//            Files.copy(file.getInputStream(), filePath);
//
//            // Generate encryption key for media
//            String encryptionKey = EncryptionUtil.generateKey();
//
//            Media media = new Media();
//            media.setMessage(message);
//            media.setUploader(uploader);
//            media.setFileUrl(filePath.toString());
//            media.setFileType(file.getContentType());
//            media.setFileSize(file.getSize());
//            media.setDurationSec(dto.getDurationSec());
//            media.setThumbnailUrl(generateThumbnail(file, uploadPath));
//            media.setEncryptionKey(encryptionKey);
//
//            mediaRepository.save(media);
//
//            return filePath.toString();
//        } catch (IOException e) {
//            throw new MediaException("Failed to upload media: " + e.getMessage());
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public Media getMedia(Long mediaId, UUID userId) {
//        Media media = mediaRepository.findById(mediaId)
//                .orElseThrow(() -> new MediaException("Media not found"));
//
//        // Validate access
//        if (media.getMessage() != null) {
//            if (!participantRepository.existsByConversationAndUser(
//                    media.getMessage().getConversation(),
//                    userRepository.findById(userId).orElseThrow()
//            )) {
//                throw new MediaException("Access denied");
//            }
//        }
//
//        return media;
//    }
//
//    private String generateThumbnail(MultipartFile file, Path uploadPath) {
//        // Implement thumbnail generation logic
//        return null; // Placeholder
//    }
////}
//@RequiredArgsConstructor
//@Service
//public class MediaService {
//
//    @Value("${media.upload.dir}")
//    private String uploadDir;
//
//    private final MediaRepository mediaRepository;
//    private final MessageRepository messageRepository;
//    private final UserRepository userRepository;
//    private final ParticipantRepository participantRepository;
//
//    @Transactional
//    public String uploadMedia(MediaUploadDto dto, MultipartFile file, UUID uploaderId) {
//        User uploader = userRepository.findById(uploaderId)
//                .orElseThrow(() -> new MediaException("Uploader not found"));
//
//        Message message = null;
//        if (dto.getMessageId() != null) {
//            message = messageRepository.findById(dto.getMessageId())
//                    .orElseThrow(() -> new MediaException("Message not found"));
//        }
//
//        try {
//            Path uploadPath = Paths.get(uploadDir);
//            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
//
//            String safeName = java.util.Objects.toString(file.getOriginalFilename(), "file.bin").replaceAll("[^a-zA-Z0-9._-]", "_");
//            String filename = UUID.randomUUID() + "_" + safeName;
//            Path filePath = uploadPath.resolve(filename);
//            Files.copy(file.getInputStream(), filePath);
//
//            Media media = new Media();
//            media.setMessage(message);
//            media.setUploader(uploader);
//            media.setFileUrl(filePath.toString());
//            media.setFileType(file.getContentType());
//            media.setFileSize(file.getSize());
//            media.setDurationSec(dto.getDurationSec());
//            media.setThumbnailUrl(generateThumbnail(file, uploadPath));
//            media.setEncryptionKey(null); // إن احتجت تشفير ملفات وسطي، طبّقه هنا
//
//            mediaRepository.save(media);
//            return filePath.toString();
//        } catch (IOException e) {
//            throw new MediaException("Failed to upload media: " + e.getMessage());
//        }
//    }
//
//    @Transactional
//    public void attachMediaToMessage(Message message, MediaAttachmentDto dto) {
//        Media media = new Media();
//        media.setMessage(message);
//        media.setUploader(message.getSender());
//        media.setFileUrl(dto.getFileUrl());
//        media.setFileType(dto.getType());
////        media.setFileSize(dto.getFileSize());
////        media.setDurationSec(dto.getDurationSec());
//        media.setThumbnailUrl(dto.getThumbnailUrl());
//        media.setEncryptionKey(dto.getEncryptionKey());
//        mediaRepository.save(media);
//    }
//
//    @Transactional(readOnly = true)
//    public Media getMedia(Long mediaId, UUID userId) {
//        Media media = mediaRepository.findById(mediaId)
//                .orElseThrow(() -> new MediaException("Media not found"));
//
//        if (media.getMessage() != null) {
//            boolean allowed = participantRepository.existsByConversationAndUser(
//                    media.getMessage().getConversation(),
//                    userRepository.findById(userId).orElseThrow()
//            );
//            if (!allowed) throw new MediaException("Access denied");
//        }
//        return media;
//    }
//
//    private String generateThumbnail(MultipartFile file, Path uploadPath) {
//        // TODO: توليد صورة مصغّرة للفيديو/الصورة إن رغبت
//        return null;
//    }
//}
@RequiredArgsConstructor
@Service
public class MediaService {

    @Value("${media.upload-dir}")
    private String uploadDir;

    private final MediaRepository mediaRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public String uploadMedia(MediaUploadDto dto, MultipartFile file, UUID uploaderId) {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new MediaException("Uploader not found"));

        Message message = null;
        if (dto.getMessageId() != null) {

                UUID messageId = UUID.fromString(dto.getMessageId());
                message = messageRepository.findById(messageId)
                        .orElseThrow(() -> new MediaException("Message not found"));


//            message = messageRepository.findById(dto.getMessageId())
//                    .orElseThrow(() -> new MediaException("Message not found"));
        }

        try {
            // ✅ تنظيم المجلدات: {uploadDir}/{type}/{yyyy-MM-dd}/
            String today = java.time.LocalDate.now().toString();
            Path uploadPath = Paths.get(uploadDir, dto.getType(), today);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // ✅ اسم ملف آمن + UUID
            String safeName = java.util.Objects
                    .toString(file.getOriginalFilename(), "file.bin")
                    .replaceAll("[^a-zA-Z0-9._-]", "_");
            String filename = UUID.randomUUID() + "_" + safeName;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // ✅ بناء كائن Media
            Media media = new Media();
            media.setMessage(message);
            media.setUploader(uploader);
            media.setFileUrl(filePath.toString());
            media.setFileType(file.getContentType());
            media.setFileSize(file.getSize());
            media.setDurationSec(dto.getDurationSec());
            media.setThumbnailUrl(generateThumbnail(file, uploadPath));
            media.setEncryptionKey(null); // تقدر تطبق تشفير هنا لاحقًا

            mediaRepository.save(media);

            return filePath.toString();
        } catch (IOException e) {
            throw new MediaException("Failed to upload media: " + e.getMessage());
        }
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

    private String generateThumbnail(MultipartFile file, Path uploadPath) {
        // TODO: تقدر تستخدم FFmpeg أو Thumbnailator
        // مثال بسيط: رجع null حالياً
        return null;
    }
}
