package com.nova.poneglyph.dto.authDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
class LoginRequestDto {
    @NotBlank
    private String phone;

    @NotBlank
    private String password;

    @NotBlank
    private String deviceId;
}
