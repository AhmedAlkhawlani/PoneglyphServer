////package com.nova.poneglyph.service;
////
////import com.nova.poneglyph.domain.user.User;
////import com.nova.poneglyph.domain.user.UserDevice;
////import com.nova.poneglyph.repository.UserDeviceRepository;
////import com.nova.poneglyph.repository.UserRepository;
////import com.nova.poneglyph.repository.UserSessionRepository;
////import lombok.RequiredArgsConstructor;
////import org.springframework.security.core.userdetails.UserDetails;
////import org.springframework.security.core.userdetails.UserDetailsService;
////import org.springframework.security.core.userdetails.UsernameNotFoundException;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////import java.time.OffsetDateTime;
////import java.util.UUID;
////
////@Service
////@RequiredArgsConstructor
////public class UserService implements UserDetailsService {
////
////    private final UserRepository userRepository;
////    private final UserDeviceRepository deviceRepository;
////    private final UserSessionRepository sessionRepository;
////
////    @Override
////    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
////        return userRepository.findById(UUID.fromString(userId))
////                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
////    }
////
////    @Transactional
////    public UserDevice registerDevice(UUID userId, DeviceRegistrationDto dto) {
////        User user = userRepository.findById(userId)
////                .orElseThrow(() -> new RuntimeException("User not found"));
////
////        UserDevice device = UserDevice.builder()
////                .user(user)
////                .deviceId(dto.getDeviceId())
////                .deviceName(dto.getDeviceName())
////                .deviceModel(dto.getDeviceModel())
////                .osVersion(dto.getOsVersion())
////                .appVersion(dto.getAppVersion())
////                .ipAddress(dto.getIpAddress())
////                .lastLogin(OffsetDateTime.now())
////                .active(true)
////                .build();
////
////        return deviceRepository.save(device);
////    }
////
////    @Transactional
////    public void updateLastActive(UUID userId) {
////        userRepository.findById(userId).ifPresent(user -> {
////            user.setLastActive(OffsetDateTime.now());
////            userRepository.save(user);
////        });
////    }
////
////    @Transactional
////    public void revokeSession(UUID sessionId) {
////        sessionRepository.findById(sessionId).ifPresent(session -> {
////            session.setRevokedAt(OffsetDateTime.now());
////            sessionRepository.save(session);
////        });
////    }
////}
//package com.nova.poneglyph.service;
//
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.domain.user.UserDevice;
//import com.nova.poneglyph.domain.user.UserSession;
//
//import com.nova.poneglyph.dto.userDto.DeviceRegistrationDto;
//import com.nova.poneglyph.repository.UserDeviceRepository;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.repository.UserSessionRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.time.OffsetDateTime;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class UserService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//    private final UserDeviceRepository deviceRepository;
//    private final UserSessionRepository sessionRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
//        return userRepository.findById(UUID.fromString(userId))
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//    }
//
//    @Transactional
//    public UserDevice registerDevice(UUID userId, DeviceRegistrationDto dto) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        UserDevice device = UserDevice.builder()
//                .user(user)
//                .deviceId(dto.getDeviceId())
//                .deviceName(dto.getDeviceName())
//                .deviceModel(dto.getDeviceModel())
//                .osVersion(dto.getOsVersion())
//                .appVersion(dto.getAppVersion())
//                .ipAddress(dto.getIpAddress())
//                .lastLogin(OffsetDateTime.now())
//                .active(true)
//                .build();
//
//        // تحديث عدد الأجهزة للمستخدم
//        user.setDeviceCount(user.getDeviceCount() + 1);
//        userRepository.save(user);
//
//        return deviceRepository.save(device);
//    }
//
//    @Transactional
//    public void updateLastActive(UUID userId) {
//        userRepository.findById(userId).ifPresent(user -> {
//            user.setLastActive(OffsetDateTime.now());
//            userRepository.save(user);
//        });
//    }
//
//    @Transactional
//    public void recordSuccessfulLogin(UUID userId) {
//        userRepository.findById(userId).ifPresent(user -> {
//            user.setLastLogin(OffsetDateTime.now());
//            user.setLoginCount(user.getLoginCount() + 1);
//            user.setFailedLoginAttempts(0);
//            userRepository.save(user);
//        });
//    }
//
//    @Transactional
//    public void recordFailedLogin(UUID userId) {
//        userRepository.findById(userId).ifPresent(user -> {
//            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
//            userRepository.save(user);
//        });
//    }
//
//    @Transactional
//    public void updateOnlineStatus(UUID userId, boolean online, String websocketSessionId) {
//        userRepository.findById(userId).ifPresent(user -> {
//            user.setOnline(online);
//            user.setWebsocketSessionId(websocketSessionId);
//            userRepository.save(user);
//        });
//    }
//
//    @Transactional
//    public void revokeSession(UUID sessionId) {
//        sessionRepository.findById(sessionId).ifPresent(session -> {
//            session.setRevokedAt(OffsetDateTime.now());
//            sessionRepository.save(session);
//        });
//    }
//}
package com.nova.poneglyph.service;

import com.nova.poneglyph.domain.enums.AccountStatus;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.domain.user.UserDevice;

import com.nova.poneglyph.dto.userDto.DeviceRegistrationDto;
import com.nova.poneglyph.repository.UserDeviceRepository;
import com.nova.poneglyph.repository.UserRepository;
import com.nova.poneglyph.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService   {

    private final UserRepository userRepository;
    private final UserDeviceRepository deviceRepository;
    private final UserSessionRepository sessionRepository;

    /**
     * loadUserByUsername: هنا نفترض أن الوسيط هو userId (كما في كود JWT الحالي).
     * إن كان كيان User يطبق UserDetails فارجع User مباشرة. وإلا أنشئ UserDetails خفيف.
     */
//    @Override
//    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
//        UUID uid;
//        try {
//            uid = UUID.fromString(userId);
//        } catch (Exception ex) {
//            throw new UsernameNotFoundException("Invalid user id format");
//        }
//
//        User user = userRepository.findById(uid)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        // إذا كان كيان User يطبّق UserDetails، يمكنك إرجاعه مباشرة:
//        if (user instanceof UserDetails) {
//            return (UserDetails) user;
//        }
//
//        // خلاف ذلك، ننشئ UserDetails خفيف يلبي متطلبات Spring Security (username = userId)
//        return org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
//                .password("") // لا حاجة لكلمة مرور هنا (نظامك يعتمد على JWT/OTP)
//                .authorities(List.of()) // ضع الأدوار لو عندك
//                .accountLocked(user.getAccountStatus() != null && user.getAccountStatus().name().equals("BANNED"))
//                .accountExpired(false)
//                .credentialsExpired(false)
//                .disabled(false)
//                .build();
//    }
//    @Override
//    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
//        UUID uid;
//        try {
//            uid = UUID.fromString(userId);
//        } catch (Exception ex) {
//            throw new UsernameNotFoundException("Invalid user id format");
//        }
//
//        User user = userRepository.findById(uid)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        // إذا كان كيان User يطبّق UserDetails، إرجاعه مباشرة
//        if (user instanceof UserDetails) {
//            return (UserDetails) user;
//        }
//
//        // بناء authorities إن وُجدت (مثال، لو عندك حقل roles في User)
//        List<org.springframework.security.core.GrantedAuthority> authorities = List.of();
//        // مثال تحويل أدوار:
//        // authorities = user.getRoles().stream()
//        //     .map(r -> new SimpleGrantedAuthority(r.getName()))
//        //     .collect(Collectors.toList());
//
//        boolean accountLocked = user.getAccountStatus() == AccountStatus.BANNED;
//        boolean disabled = user.getAccountStatus() != AccountStatus.ACTIVE;
//
//        // نستخدم builder من Spring Security لإنشاء UserDetails "خَفيف"
//        return org.springframework.security.core.userdetails.User
//                .withUsername(user.getId().toString())
//                .password("") // لا نستخدم كلمة مرور لأنّنا نعتمد على JWT/OTP
//                .authorities(authorities)
//                .accountLocked(accountLocked)
//                .accountExpired(false)
//                .credentialsExpired(false)
//                .disabled(disabled)
//                .build();
//    }

    /**
     * تسجيل جهاز: نتأكد ألا نكرّر وجود نفس الجهاز للمستخدم.
     * - نُحدّث السجل إن وُجد.
     * - نُنشئ سجلًا جديدًا وإضافة deviceCount فقط عند الإنشاء.
     */
    @Transactional
    public UserDevice registerDevice(UUID userId, DeviceRegistrationDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // حاول إيجاد جهاز مشابه للمستخدم
        Optional<UserDevice> existingOpt = deviceRepository.findByUser_IdAndDeviceId(userId, dto.getDeviceId());

        if (existingOpt.isPresent()) {
            // حدث معلومات الجهاز ووقت الدخول الأخير
            UserDevice device = existingOpt.get();
            device.setDeviceName(dto.getDeviceName());
            device.setDeviceModel(dto.getDeviceModel());
            device.setOsVersion(dto.getOsVersion());
            device.setAppVersion(dto.getAppVersion());
            device.setIpAddress(dto.getIpAddress());
            device.setLastLogin(OffsetDateTime.now());
            device.setActive(true);
            return deviceRepository.save(device);
        } else {
            // انشئ جهاز جديد
            UserDevice device = UserDevice.builder()
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

            // زيادة deviceCount بطريقة آمنة (optimistic locking موجود عبر @Version)
            user.setDeviceCount(user.getDeviceCount() + 1);
            userRepository.save(user);

            return deviceRepository.save(device);
        }
    }

    @Transactional
    public void updateLastActive(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastActive(OffsetDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void recordSuccessfulLogin(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(OffsetDateTime.now());
            user.setLoginCount(user.getLoginCount() + 1);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        });
    }

    @Transactional
    public void recordFailedLogin(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            userRepository.save(user);
        });
    }

    @Transactional
    public void updateOnlineStatus(UUID userId, boolean online, String websocketSessionId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnline(online);
            user.setWebsocketSessionId(websocketSessionId);
            userRepository.save(user);
        });
    }

    /**
     * إبطال جلسة واحدة: استخدم دالة revoke() الموجودة في الكيان UserSession
     * بدل تعديل الـ fields يدوياً.
     */
    @Transactional
    public void revokeSession(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.revoke(); // sets revokedAt + active = false
            sessionRepository.save(session);
        });
    }
}
