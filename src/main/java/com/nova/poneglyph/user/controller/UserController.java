package com.nova.poneglyph.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.poneglyph.mapper.UserMapper;
import com.nova.poneglyph.user.dto.AuthUserDto;
import com.nova.poneglyph.user.dto.UserDto;
import com.nova.poneglyph.user.request.RegisterRequest;
import com.nova.poneglyph.user.request.UserUpdateRequest;
import com.nova.poneglyph.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

//    @PostMapping(value ="/update",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
////    @PreAuthorize("hasAnyAuthority('ADMIN') or @userService.getUserById(#request.id).username == principal")
//    public ResponseEntity<UserDto> updateUserById(@Valid @RequestPart("request") UserUpdateRequest request,
//                                                  @RequestPart(value = "file", required = false) MultipartFile file) {
//        System.out.println("Username: " + request.getUsername());
//
//        return ResponseEntity.ok(UserMapper.toDTO(userService.updateUserById(request, file), true));
//    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateUserById(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        UserUpdateRequest request = mapper.readValue(requestJson, UserUpdateRequest.class);
        System.out.println("Username: " + request.getUsername());

        // باقي المعالجة
        return ResponseEntity.ok(UserMapper.toDTO(userService.updateUserById(request, file), true));


    }
    @PostMapping("/save")
    public ResponseEntity<UserDto> save(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(UserMapper.toDTO(userService.saveUser(request),false));
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(UserMapper.dtoList(userService.getAll(),true));
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(UserMapper.toDTO(userService.getUserById(id),true));
    }
    @GetMapping("/findUserByPhone/{phone}")
    public ResponseEntity<UserDto> getUserByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(UserMapper.toDTO(userService.getUserByUsername(phone),true));
    }

    @GetMapping("/getUserByEmail/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(UserMapper.toDTO(userService.getUserByEmail(email), true));
    }

    @GetMapping("/getUserByUsername/{username}")
    public ResponseEntity<AuthUserDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(UserMapper.toDTOAuth(userService.getUserByUsername(username)));
    }



    @DeleteMapping("/deleteUserById/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).username == principal")
    public ResponseEntity<Void> deleteUserById(@PathVariable String id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }
}
