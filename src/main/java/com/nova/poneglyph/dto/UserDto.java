//package com.nova.poneglyph.dto;
//
//
//
//import jakarta.validation.constraints.NotBlank;
//import java.util.UUID;
//
//public class UserDto {
//
//    public record UserProfileDto(
//            UUID userId,
//            String displayName,
//            String avatarUrl,
//            String aboutText,
//            String statusEmoji
//    ) {}
//
//    public record UserSettingsDto(
//            boolean messageNotifications,
//            boolean callNotifications,
//            boolean groupNotifications,
//            boolean onlineStatusVisible,
//            boolean readReceipts,
//            String theme,
//            String language
//    ) {}
//
//    public record DeviceRegistrationDto(
//            @NotBlank String deviceId,
//            String deviceName,
//            String deviceModel,
//            String osVersion,
//            String appVersion,
//            String ipAddress
//    ) {}
//}
