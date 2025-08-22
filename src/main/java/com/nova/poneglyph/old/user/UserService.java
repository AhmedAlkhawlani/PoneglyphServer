//package com.nova.poneglyph.service.impl.user;
//
//import com.nova.poneglyph.service.impl.StorageService;
//import com.nova.poneglyph.mapper.UserDetailsMapper;
//
//import com.nova.poneglyph.dto.UserDetailsDto;
//import com.nova.poneglyph.enums.old.user.enums.Active;
//import com.nova.poneglyph.enums.old.user.enums.Role;
//import com.nova.poneglyph.exception.user.exc.NotFoundException;
//import com.nova.poneglyph.model.User;
//import com.nova.poneglyph.model.UserProfile;
//import com.nova.poneglyph.repository.UserRepository;
//import com.nova.poneglyph.dto.request.RegisterRequest;
//import com.nova.poneglyph.dto.request.UserUpdateRequest;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@Transactional
//@Service
//@RequiredArgsConstructor
//public class UserService {
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final StorageService storageService;
////    private final FileStorageClient fileStorageClient;
////    private final UserMapper userMapper;
//
//    public User saveUser(RegisterRequest request) {
//        User toSave = User.builder()
//                .phoneNumber(request.getUsername())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .email(request.getEmail())
//                .role(Role.USER)
//                .active(Active.ACTIVE).build();
//        return userRepository.save(toSave);
//    }
//
//    public List<User> getAll() {
//        return userRepository.findAllByActive(Active.ACTIVE);
//    }
//
//    public User getUserById(String id) {
//        return findUserById(id);
//    }
//
//    public User getUserByEmail(String email) {
//        return findUserByEmail(email);
//    }
//
//    public User getUserByUsername(String username) {
//        return findUserByUsername(username);
//    }
//
////    public User updateUserById(UserUpdateRequest request, MultipartFile file) {
////        User toUpdate = findUserById(request.getId());
////
////        request.setUserDetails(updateUserDetails(toUpdate.getProfile(), request.getUserDetails(), file));
////        UserMapper.map(request, toUpdate);
////
////        return userRepository.save(toUpdate);
////    }
//public User updateUserById(UserUpdateRequest request, MultipartFile file) {
//    User toUpdate = findUserById(request.getId());
//
//    // إذا لم يكن موجودًا، أنشئ كائن جديد
//    if (toUpdate.getProfile() == null) {
//        toUpdate.setProfile(new UserProfile());
//    }
//
//    // تحديث الخصائص
//    updateUserDetails(toUpdate.getProfile(), request.getUserDetails(), file);
//
////    UserMapper.map(request, toUpdate);
//    if (request.getPassword() != null) {
//
//        toUpdate.setPassword(passwordEncoder.encode(request.getPassword()));
//
//    }
//    return userRepository.save(toUpdate);
//}
//
//
//
//    public void deleteUserById(String id) {
//        User toDelete = findUserById(id);
//        toDelete.setActive(Active.INACTIVE);
//        userRepository.save(toDelete);
//    }
//
//    protected User findUserById(String id) {
//        return userRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//    }
//
//    protected User findUserByEmail(String email) {
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//    }
//
//    protected User findUserByUsername(String username) {
//        return userRepository.findByPhoneNumber(username)
//                .orElseThrow(() -> new NotFoundException("phoneNumber User not found"));
//    }
//
////    private void updateUserDetails(UserProfile toUpdate, UserDetailsDto request, MultipartFile file) {
////        if (file != null && !file.isEmpty()) {
////            String newProfilePicture = storageService.uploadImageToFileSystem(file);
////
////            if (newProfilePicture != null) {
////                // ✅ تحقق من وجود صورة حالية قبل الحذف
////                String currentPicture = toUpdate.getProfilePicture();
////                if (currentPicture != null && !currentPicture.isBlank()) {
////                    storageService.deleteImageFromFileSystem(currentPicture);
////                }
////
////                toUpdate.setProfilePicture(newProfilePicture);
////            }
////        }
////
////        UserDetailsMapper.map(request, toUpdate);
////    }
//private void updateUserDetails(UserProfile toUpdate, UserDetailsDto request, MultipartFile file) {
//
//    // 1️⃣ رفع صورة جديدة إن وُجدت
//    if (file != null && !file.isEmpty()) {
//        String oldImage = toUpdate.getProfilePicture();
//        String fileName = storageService.uploadImageToFileSystem(file);
//        toUpdate.setProfilePicture(fileName);
//
//        if (oldImage != null && !oldImage.isBlank()) {
//            storageService.deleteImageFromFileSystem(oldImage);
//        }
//    }
//
//    // 2️⃣ حذف الصورة إذا طلب المستخدم ذلك
//    else if ("DELETE".equalsIgnoreCase(request.getProfilePicture())) {
//        String currentPicture = toUpdate.getProfilePicture();
//        if (currentPicture != null && !currentPicture.isBlank()) {
//            storageService.deleteImageFromFileSystem(currentPicture);
//            toUpdate.setProfilePicture(null);
//        }
//    }
//
//    // 3️⃣ تحديث باقي الحقول
//    UserDetailsMapper.mapWithoutProfilePicture(request, toUpdate);  // سنعدّل هذا لاحقًا
//}
//
//
//}
