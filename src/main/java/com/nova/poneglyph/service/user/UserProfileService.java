package com.nova.poneglyph.service.user;

import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserProfile;
import com.nova.poneglyph.dto.userDto.UserProfileDto;
import com.nova.poneglyph.exception.UserException;
import com.nova.poneglyph.repository.UserProfileRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.service.media.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public String uploadProfileImage(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        // استخدام FileStorageService لرفع الصورة
        String fileId = fileStorageService.storeFile(file, userId);

        // الحصول على الملف الشخصي أو إنشاء جديد
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setAvatarUrl(fileId); // تخزين معرف الملف بدلاً من المسار الكامل
        profile.setLastProfileUpdate(OffsetDateTime.now());
        userProfileRepository.save(profile);

        return fileId;
    }

    @Transactional
    public UserProfileDto updateProfile(UUID userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        // الحصول على الملف الشخصي أو إنشاء جديد
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setDisplayName(profileDto.getDisplayName());
        profile.setAboutText(profileDto.getAboutText());
        profile.setStatusEmoji(profileDto.getStatusEmoji());
        profile.setLastProfileUpdate(OffsetDateTime.now());

        if (profileDto.getAvatarUrl() != null) {
            profile.setAvatarUrl(profileDto.getAvatarUrl());
        }

        UserProfile savedProfile = userProfileRepository.save(profile);
        return mapToDto(savedProfile);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(UUID userId) {
        // التحقق من وجود المستخدم أولاً
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));

        // البحث عن الملف الشخصي أو إنشاء DTO افتراضي
        Optional<UserProfile> profileOptional = userProfileRepository.findById(userId);

        if (profileOptional.isPresent()) {
            return mapToDto(profileOptional.get());
        } else {
            // إرجاع ملف تعريف افتراضي إذا لم يتم العثور عليه
            return createDefaultProfileDto(user);
        }
    }

    private UserProfileDto createDefaultProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(user.getId());
        dto.setDisplayName(user.getDisplayName()); // استخدام اسم العرض الافتراضي من User
        dto.setAvatarUrl(null); // لا توجد صورة افتراضية
        dto.setAboutText(""); // نص حول فارغ
        dto.setStatusEmoji(""); // إيموجي حالة فارغ
        dto.setLastProfileUpdate(null); // لم يتم التحديث بعد
        return dto;
    }

    private UserProfileDto mapToDto(UserProfile profile) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(profile.getUser().getId());
        dto.setDisplayName(profile.getDisplayName());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setAboutText(profile.getAboutText());
        dto.setStatusEmoji(profile.getStatusEmoji());
        dto.setLastProfileUpdate(profile.getLastProfileUpdate());
        return dto;
    }
}
