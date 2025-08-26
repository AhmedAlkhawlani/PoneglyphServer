package com.nova.poneglyph.dto.userDto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class UserDeviceDto {
    private UUID id;
    private String deviceId;
    private String deviceName;
    private String deviceModel;
    private String osVersion;
    private String appVersion;
    private String ipAddress;
    private OffsetDateTime lastLogin;
    private boolean active;
}
