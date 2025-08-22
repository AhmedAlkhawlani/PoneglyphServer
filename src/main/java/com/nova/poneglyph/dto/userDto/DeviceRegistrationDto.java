package com.nova.poneglyph.dto.userDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceRegistrationDto {
    @NotBlank
    private String deviceId;

    private String deviceName;
    private String deviceModel;
    private String osVersion;
    private String appVersion;
    private String ipAddress;
}
