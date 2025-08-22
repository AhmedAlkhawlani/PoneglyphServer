//package com.nova.poneglyph.api.controller.integration;
//
//
//
//import com.nova.poneglyph.dto.authDto.OtpRequestDto;
//import com.nova.poneglyph.dto.authDto.OtpVerifyDto;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class AuthControllerIT {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void testOtpFlow() throws Exception {
//        // Step 1: Request OTP
//        OtpRequestDto requestDto = new OtpRequestDto(
//                "+966501234567",
//                "device123",
//                "fingerprint123",
//                "192.168.1.1"
//        );
//
//        mockMvc.perform(post("/api/auth/otp/request")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(requestDto)))
//                .andExpect(status().isOk());
//
//        // Step 2: Verify OTP (assuming code is 123456)
//        OtpVerifyDto verifyDto = new OtpVerifyDto(
//                "+966501234567",
//                "123456",
//                "device123",
//                "fingerprint123",
//                "192.168.1.1"
//        );
//
//        mockMvc.perform(post("/api/auth/otp/verify")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(verifyDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").exists())
//                .andExpect(jsonPath("$.refreshToken").exists());
//    }
//
//    private static String asJsonString(final Object obj) {
//        try {
//            return new ObjectMapper().writeValueAsString(obj);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
