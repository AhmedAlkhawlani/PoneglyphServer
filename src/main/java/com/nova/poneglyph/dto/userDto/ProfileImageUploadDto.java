package com.nova.poneglyph.dto.userDto;



import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileImageUploadDto {
    private MultipartFile file;
}
