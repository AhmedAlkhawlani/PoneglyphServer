package com.nova.poneglyph.dto.authDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class RefreshRequestDto {
    @NotBlank
    private String refreshToken;
}
