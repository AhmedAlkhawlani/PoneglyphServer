//package com.nova.poneglyph.dto;
//
//
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Pattern;
//
//public class AuthDto {
//
//    public record com.nova.poneglyph.dto.OtpRequestDto(
//            @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phone,
//            @NotBlank String deviceId,
//            String deviceFingerprint,
//            String ip
//    ) {}
//
//    public record com.nova.poneglyph.dto.OtpVerifyDto(
//            @NotBlank String phone,
//            @NotBlank String code,
//            @NotBlank String deviceId,
//            String deviceFingerprint,
//            String ip
//    ) {}
//
//    public record com.nova.poneglyph.dto.AuthResponseDto(
//            String accessToken,
//            long accessExpiresIn,
//            String refreshToken,
//            long refreshExpiresIn,
//            String userId
//    ) {}
//
//    public record com.nova.poneglyph.dto.RefreshRequestDto(
//            @NotBlank String refreshToken
//    ) {}
//}
