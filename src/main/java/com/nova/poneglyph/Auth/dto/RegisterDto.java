package com.nova.poneglyph.Auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class RegisterDto {
    private String id;
    private String username;
    private String email;
}
