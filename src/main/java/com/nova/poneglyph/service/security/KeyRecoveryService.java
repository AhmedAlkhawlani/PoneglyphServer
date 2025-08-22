//package com.nova.poneglyph.service.security;
//
//
//import com.nova.poneglyph.domain.user.User;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.util.EncryptionUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class KeyRecoveryService {
//
//    private final UserRepository userRepository;
//    private final EncryptionUtil encryptionUtil;
//
//    @Transactional
//    public void backupKeys(UUID userId, String privateKey, String recoveryPassword) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
////        String encryptedPrivateKey = encryptionUtil.encryptWithPassword(
////                privateKey,
////                recoveryPassword
////        );
//
////        user.setEncryptedPrivateKey(encryptedPrivateKey);
//        userRepository.save(user);
//    }
//
//    @Transactional
//    public String restoreKeys(UUID userId, String recoveryPassword) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        return encryptionUtil.decryptWithPassword(
//                user.getEncryptedPrivateKey(),
//                recoveryPassword
//        );
//    }
//}
