package com.nova.poneglyph.api.controller.user;

import com.nova.poneglyph.config.v2.CustomUserDetails;
import com.nova.poneglyph.dto.userDto.DeviceRegistrationDto;
import com.nova.poneglyph.dto.userDto.UserDeviceDto;
import com.nova.poneglyph.exception.AuthorizationException;
import com.nova.poneglyph.service.user.UserDeviceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/devices")
@RequiredArgsConstructor
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    @PostMapping
    public ResponseEntity<UserDeviceDto> registerDevice(
            @RequestBody DeviceRegistrationDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        UserDeviceDto device = userDeviceService.registerDevice(userId, dto);
        return ResponseEntity.ok(device);
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> revokeDevice(
            @PathVariable UUID deviceId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        userDeviceService.revokeDevice(userId, deviceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDeviceDto>> getUserDevices(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            throw new AuthorizationException("Unauthenticated");
        }

        UUID userId = userDetails.getId(); // استخدم الـ UUID مباشرة من CustomUserDetails

        List<UserDeviceDto> devices = userDeviceService.getUserDevices(userId);
        return ResponseEntity.ok(devices);
    }
}
