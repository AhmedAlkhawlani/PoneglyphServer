
package com.nova.poneglyph.api.controller.media;
import com.nova.poneglyph.service.media.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
@RestController
@RequestMapping("/v1/file-storage")
@RequiredArgsConstructor
public class FileStorageController {
    private final FileStorageService fileStorageService;
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UUID userId) {
        try {
            String fileId = fileStorageService.storeFile(file, userId);
            return ResponseEntity.ok(fileId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to upload file: " + e.getMessage());
        }
    }
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileId,
            @AuthenticationPrincipal UUID userId) {
        try {
            FileStorageService.FileResource fileResource = fileStorageService.loadFileAsResource(fileId, userId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileResource.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileResource.getFilename() + "\"")
                    .body(fileResource.getResource());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable String fileId,
            @AuthenticationPrincipal UUID userId) {
        try {
            fileStorageService.deleteFile(fileId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
