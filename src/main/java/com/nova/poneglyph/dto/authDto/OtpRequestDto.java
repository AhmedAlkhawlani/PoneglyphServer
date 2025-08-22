package com.nova.poneglyph.dto.authDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpRequestDto {
    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$")
    private String phone;

    @NotBlank
    private String deviceId;

    private String deviceFingerprint;
    private String ip;
}
