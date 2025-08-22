package com.nova.poneglyph.service.admin;



import com.nova.poneglyph.domain.enums.AccountStatus;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public void banUser(UUID userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(AccountStatus.BANNED);
        user.setBanReason(reason);
        user.setBanExpiry(null); // حظر دائم

        userRepository.save(user);
    }

    @Transactional
    public void unbanUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setBanReason(null);
        user.setBanExpiry(null);

        userRepository.save(user);
    }
}
