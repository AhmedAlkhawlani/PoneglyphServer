//package com.nova.poneglyph.file;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@RestController
//@RequestMapping("/api/files")
//public class FileController {
//
//    @Value("${application.file.uploads.media-output-path}")
//    private String fileUploadPath;
//
//    @GetMapping("/users/{userId}/{filename:.+}")
//    public ResponseEntity<Resource> getFile(
//            @PathVariable String userId,
//            @PathVariable String filename) throws IOException {
//
//        Path filePath = Paths.get(fileUploadPath)
//                .resolve("users")
//                .resolve(userId)
//                .resolve(filename);
//
//        Resource resource = new UrlResource(filePath.toUri());
//
//        if (resource.exists() && resource.isReadable()) {
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
//                    .contentType(MediaType.IMAGE_JPEG) // أو تحديده ديناميكياً
//                    .body(resource);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//}
