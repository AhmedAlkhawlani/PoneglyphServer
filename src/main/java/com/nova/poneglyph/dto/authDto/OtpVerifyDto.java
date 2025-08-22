package com.nova.poneglyph.dto.authDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class OtpVerifyDto {
    @NotBlank
    private String phone;

    @NotBlank
    private String code;

    @NotBlank
    private String deviceId;

    private String deviceFingerprint;
    private String ip;
}
