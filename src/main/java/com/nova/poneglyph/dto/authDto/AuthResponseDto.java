package com.nova.poneglyph.dto.authDto;
import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class AuthResponseDto {
    private String accessToken;
    private long accessExpiresIn;
    private String refreshToken;
    private long refreshExpiresIn;
    private String userId;
}
