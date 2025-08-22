package com.nova.poneglyph.config.v2;



import com.nova.poneglyph.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String phoneNumber;
    private final boolean verified;
    private final User user; // الوصول الكامل لكل الحقول عند الحاجة

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.phoneNumber = user.getPhoneNumber();
        this.verified = user.isVerified();
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // إذا لديك Roles أو Permissions أضفها هنا
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return null; // إذا تستخدم OTP فقط، لا تحتاج كلمة سر
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getAccountStatus() != null && user.getAccountStatus().name().equals("ACTIVE");
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getAccountStatus() != null && user.getAccountStatus().name().equals("ACTIVE");
    }
}

