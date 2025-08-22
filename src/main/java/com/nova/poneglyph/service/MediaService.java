//package com.nova.poneglyph.service;
//
//
//
//import com.nova.poneglyph.domain.message.Media;
//import com.nova.poneglyph.domain.message.Message;
//import com.nova.poneglyph.dto.MediaUploadDto;
//import com.nova.poneglyph.exception.MediaException;
//import com.nova.poneglyph.repository.MediaRepository;
//import com.nova.poneglyph.repository.MessageRepository;
//import com.nova.poneglyph.util.EncryptionUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class MediaService {
//
//    @Value("${media.upload.dir}")
//    private String uploadDir;
//
//    private final MediaRepository mediaRepository;
//    private final MessageRepository messageRepository;
//
//    @Transactional
//    public Media uploadMedia(MediaUploadDto dto, MultipartFile file) {
//        Message message = messageRepository.findById(dto.getMessageId())
//                .orElseThrow(() -> new MediaException("Message not found"));
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
//            Media media = Media.builder()
//                    .message(message)
//                    .fileUrl(filePath.toString())
//                    .fileType(file.getContentType())
//                    .fileSize(file.getSize())
//                    .encryptionKey(encryptionKey)
//                    .build();
//
//            return mediaRepository.save(media);
//        } catch (IOException e) {
//            throw new MediaException("Failed to upload media: " + e.getMessage());
//        }
//    }
//}
