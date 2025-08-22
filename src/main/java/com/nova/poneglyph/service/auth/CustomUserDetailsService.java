package com.nova.poneglyph.service.auth;



import com.nova.poneglyph.config.v2.CustomUserDetails;
import com.nova.poneglyph.domain.user.User;
import com.nova.poneglyph.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdStr) throws UsernameNotFoundException {
        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("Invalid user ID format: " + userIdStr);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        return new CustomUserDetails(user);
    }

}

