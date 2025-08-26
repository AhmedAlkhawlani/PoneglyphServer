package com.nova.poneglyph.service.user;



import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;
import com.nova.poneglyph.dto.userDto.DeviceRegistrationDto;
import com.nova.poneglyph.dto.userDto.UserDeviceDto;
import com.nova.poneglyph.exception.UserException;
import com.nova.poneglyph.repository.UserDeviceRepository;
import com.nova.poneglyph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserDeviceDto registerDevice(UUID userId, DeviceRegistrationDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        UserDevice existingDevice = userDeviceRepository.findByUserAndDeviceId(user, dto.getDeviceId())
                .orElse(null);

        UserDevice device;
        if (existingDevice != null) {
            device = existingDevice;
            device.setDeviceName(dto.getDeviceName());
            device.setDeviceModel(dto.getDeviceModel());
            device.setOsVersion(dto.getOsVersion());
            device.setAppVersion(dto.getAppVersion());
            device.setIpAddress(dto.getIpAddress());
            device.setLastLogin(OffsetDateTime.now());
            device.setActive(true);
        } else {
            device = UserDevice.builder()
                    .user(user)
                    .deviceId(dto.getDeviceId())
                    .deviceName(dto.getDeviceName())
                    .deviceModel(dto.getDeviceModel())
                    .osVersion(dto.getOsVersion())
                    .appVersion(dto.getAppVersion())
                    .ipAddress(dto.getIpAddress())
                    .lastLogin(OffsetDateTime.now())
                    .active(true)
                    .build();
        }

        UserDevice savedDevice = userDeviceRepository.save(device);
        return mapToDto(savedDevice);
    }

    @Transactional
    public void revokeDevice(UUID userId, UUID deviceId) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new UserException("Device not found"));

        if (!device.getUser().getId().equals(userId)) {
            throw new UserException("Access denied");
        }

        device.setActive(false);
        userDeviceRepository.save(device);
    }

    @Transactional(readOnly = true)
    public List<UserDeviceDto> getUserDevices(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        List<UserDevice> devices = userDeviceRepository.findByUser(user);
        return devices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDeviceDto mapToDto(UserDevice device) {
        UserDeviceDto dto = new UserDeviceDto();
        dto.setId(device.getId());
        dto.setDeviceId(device.getDeviceId());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceModel(device.getDeviceModel());
        dto.setOsVersion(device.getOsVersion());
        dto.setAppVersion(device.getAppVersion());
        dto.setIpAddress(device.getIpAddress());
        dto.setLastLogin(device.getLastLogin());
        dto.setActive(device.isActive());
        return dto;
    }
}
