package com.nova.poneglyph.dto.authDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpVerifyDto {
    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @NotBlank
    @Size(min = 6, max = 6, message = "OTP code must be 6 digits")
    private String code;

    @NotBlank
    private String deviceId;

    @NotBlank
    private String deviceKey;

    @NotBlank
    private String deviceFingerprint;

    private String osVersion;
    private String appVersion;

    private String ip;
    
}
