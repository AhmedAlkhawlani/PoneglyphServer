package com.nova.poneglyph.service.media;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final Path fileStorageLocation;
    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    public String storeFile(MultipartFile file, UUID userId) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Generate unique file ID
            String fileId = UUID.randomUUID().toString();
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i);
            }

            String newFileName = fileId + extension;
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileId;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
    public FileResource loadFileAsResource(String fileId, UUID userId) {
        try {
            // Find the file with the given ID
            Path filePath = findFilePath(fileId);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return new FileResource(resource, getContentType(filePath), getFileName(filePath));
            } else {
                throw new RuntimeException("File not found " + fileId);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileId, ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteFile(String fileId, UUID userId) {
        try {
            Path filePath = findFilePath(fileId);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + fileId, ex);
        }
    }
    private Path findFilePath(String fileId) throws IOException {
        return Files.list(this.fileStorageLocation)
                .filter(path -> path.getFileName().toString().startsWith(fileId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
    }
    private String getContentType(Path filePath) {
        try {
            return Files.probeContentType(filePath);
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
    private String getFileName(Path filePath) {
        return filePath.getFileName().toString();
    }
    public static class FileResource {
        private final Resource resource;
        private final String contentType;
        private final String filename;
        public FileResource(Resource resource, String contentType, String filename) {
            this.resource = resource;
            this.contentType = contentType;
            this.filename = filename;
        }
        public Resource getResource() { return resource; }
        public String getContentType() { return contentType; }
        public String getFilename() { return filename; }
    }
}
