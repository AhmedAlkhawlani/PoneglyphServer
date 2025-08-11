package com.nova.poneglyph.user.dto;

import com.nova.poneglyph.user.enums.Role;
import lombok.Data;

@Data
public class AuthUserDto {
    private String id;
    private String username;
    private String password;
    private Role role;
}
