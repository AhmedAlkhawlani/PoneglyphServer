
package com.nova.poneglyph.service;


import com.nova.poneglyph.domain.enums.AccountStatus;
import com.nova.poneglyph.domain.moderation.SystemBan;
import com.nova.poneglyph.repository.UserRepository;

import com.nova.poneglyph.repository.SystemBanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


import com.nova.poneglyph.domain.user.User;

import com.nova.poneglyph.util.PhoneUtil;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserGuardService {

    private final UserRepository userRepository;
    private final SystemBanRepository systemBanRepository;
//    private final PhoneUtil phoneUtil;

    /**
     * التحقق من إمكانية إنشاء حساب جديد
     */
    @Transactional(readOnly = true)
    public boolean canCreateAccount(String phoneNumber) {
//        String normalizedPhone = phoneUtil.normalizePhone(phoneNumber);
        String defaultRegion = PhoneUtil.extractRegionFromE164(phoneNumber);
        String normalizedPhone = PhoneUtil.normalizeForStorage(phoneNumber, defaultRegion);


        // التحقق من الحظر النظامي
        if (isBannedBySystem(normalizedPhone)) {
            throw new SecurityException("Phone number is banned from creating accounts");
        }

        // التحقق من وجود حساب سابق
        Optional<User> existingUser = userRepository.findByNormalizedPhone(normalizedPhone);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getAccountStatus() == AccountStatus.BANNED) {
                throw new SecurityException("Account is banned");
            }
            if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
                throw new SecurityException("Account is suspended");
            }
        }

        return true;
    }

    /**
     * التحقق من إمكانية إرسال الرسائل
     */
    @Transactional(readOnly = true)
    public boolean canSendMessage(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SecurityException("User not found"));

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new SecurityException("Account is not active");
        }

        if (user.getBanExpiry() != null && user.getBanExpiry().isAfter(OffsetDateTime.now())) {
            throw new SecurityException("Account is temporarily banned");
        }

        return true;
    }

    /**
     * التحقق من إمكانية إنشاء محادثة
     */
    @Transactional(readOnly = true)
    public boolean canCreateConversation(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SecurityException("User not found"));

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new SecurityException("Account is not active");
        }

        // يمكن إضافة قيود إضافية مثل الحد الأقصى للمحادثات

        return true;
    }

    /**
     * التحقق من إمكانية إجراء مكالمة
     */
    @Transactional(readOnly = true)
    public boolean canMakeCall(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SecurityException("User not found"));

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new SecurityException("Account is not active");
        }

        // التحقق من القيود الخاصة بالمكالمات

        return true;
    }

    /**
     * التحقق من الحظر النظامي للرقم
     */
    @Transactional(readOnly = true)
    public boolean isBannedBySystem(String normalizedPhone) {
        Optional<SystemBan> activeBan = systemBanRepository
                .findFirstByNormalizedPhoneAndActiveIsTrueOrderByCreatedAtDesc(normalizedPhone);

        if (activeBan.isEmpty()) {
            return false;
        }

        SystemBan ban = activeBan.get();

        // التحقق من انتهاء مدة الحظر المؤقت
        if (ban.getExpiresAt() != null && ban.getExpiresAt().isBefore(OffsetDateTime.now())) {
            ban.setActive(false);
            systemBanRepository.save(ban);
            return false;
        }

        return true;
    }

    /**
     * التحقق من حظر مستخدم من قبل مستخدم آخر
     */
    @Transactional(readOnly = true)
    public boolean isBlockedByUser(UUID blockerId, UUID blockedId) {
        return userRepository.existsBlock(blockerId, blockedId);
    }

    /**
     * التحقق من التقييد على عدد الطلبات
     */
    public boolean isRateLimited(String identifier, int maxRequests, int timeWindowMinutes) {
        // سيتم تنفيذ هذا باستخدام Redis في الإصدار الإنتاجي
        // هذا تنفيذ مبسط للتجربة
        return false;
    }

    /**
     * تسجيل محاولة دخول فاشلة
     */
    @Transactional
    public void recordFailedLoginAttempt(String phoneNumber, String ipAddress) {
//        String normalizedPhone = phoneUtil.normalizePhone(phoneNumber);
        String defaultRegion = PhoneUtil.extractRegionFromE164(phoneNumber);
        String normalizedPhone = PhoneUtil.normalizeForStorage(phoneNumber, defaultRegion);

        Optional<User> userOpt = userRepository.findByNormalizedPhone(normalizedPhone);

        userOpt.ifPresent(user -> {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            // تعليق الحساب بعد 5 محاولات فاشلة
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountStatus(AccountStatus.SUSPENDED);
                user.setBanExpiry(OffsetDateTime.now().plusHours(1));
            }

            userRepository.save(user);
        });
    }

    /**
     * إعادة تعيين عداد المحاولات الفاشلة
     */
    @Transactional
    public void resetFailedLoginAttempts(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SecurityException("User not found"));

        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }
}

// ==========================
// 12) Notes
// ==========================
// - For DB generated columns (normalized_phone, sequence_number): they are read-only here.
// - Consider writing Flyway/Liquibase migrations reflecting the SQL you have.
// - For inet type, you can store as text or add a custom Hibernate UserType.
// - For JSONB metadata, map to JsonNode with a converter if preferred.
// - Add DTOs and mappers (MapStruct) on top of entities when building APIs.
