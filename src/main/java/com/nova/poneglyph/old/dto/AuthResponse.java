//package com.nova.poneglyph.dto;
//
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class AuthResponse {
//
//    private String token;
//    private String refreshToken; // أضف هذا الحقل
//    private long expiresIn;      // أضف هذا الحقل
//    private UserDto user;
//
//    // يمكنك إضافة كونستركتور للتوافق مع الكود القديم
//    public AuthResponse(String token, UserDto user) {
//        this.token = token;
//        this.user = user;
//    }
//}
