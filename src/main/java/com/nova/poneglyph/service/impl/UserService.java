//package com.nova.poneglyph.service.impl;
//
//import com.nova.poneglyph.dto.AuthResponse;
//import com.nova.poneglyph.dto.LoginRequest;
//import com.nova.poneglyph.dto.RegisterRequest;
//import com.nova.poneglyph.dto.UserDTO;
//
//import com.nova.poneglyph.exception.ApiException;
//import com.nova.poneglyph.exception.ErrorCode;
//
//
//import com.nova.poneglyph.service.UserTokenService;
//
//import com.nova.poneglyph.utils.JWTUtils;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@Transactional
//public class UserService  {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private JWTUtils jwtUtils;
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//    @Autowired
//    private UserTokenService userTokenService;
//
//    public UserService(UserMapper userMapper) {
//        this.userMapper = userMapper;
//    }
//
//
//    public AuthResponse register(RegisterRequest user) {
//        if (userRepository.existsByName(user.getUsername())) {
//            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.USERNAME_ALREADY_EXISTS, "اسم المستخدم مستخدم بالفعل");
//        }
//
//        if (userRepository.existsByEmail(user.getEmail())) {
//            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.EMAIL_ALREADY_EXISTS, "البريد الإلكتروني مستخدم بالفعل");
//        }
//
//        if (user.getRole() == null || user.getRole().isBlank()) {
//            user.setRole("USER");
//        }
//
//        User newUser = new User();
//        newUser.setEmail(user.getEmail());
//        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
//        newUser.setName(user.getUsername());
//        newUser.setRole(user.getRole());
//
//        User savedUser = userRepository.save(newUser);
//        String token = jwtUtils.generateToken(savedUser);
//        UserResponse userDTO = userMapper.toUserResponse(savedUser);
//
//        return new AuthResponse(token, userDTO);
//    }
//
//    public AuthResponse login(LoginRequest loginRequest) {
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            loginRequest.getUsername(),
//                            loginRequest.getPassword()
//                    )
//            );
//        } catch (Exception e) {
//            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "اسم المستخدم أو كلمة المرور غير صحيحة");
//        }
//
//        User user = userRepository.findByName(loginRequest.getUsername())
//                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "المستخدم غير موجود"));
//
//        String token = jwtUtils.generateToken(user);
//        userTokenService.saveOrUpdateToken(user.getId(), token, jwtUtils.getExpiration(token));
//
//        UserResponse userResponse = userMapper.toUserResponse(user);
//        return new AuthResponse(token, userResponse);
//    }
//
////
////    public UserResponse getAllUsers() {
////        UserResponse response = new UserResponse();
////
////        try {
////            List<User> userList = userRepository.findAll();
////            List<UserDTO> userDTOList = Utils.mapUserListEntityToUserListDTO(userList);
////
////            response.setUserList(userDTOList);
////            response.setMessage("successful");
////            response.setStatusCode(200);
////
////        } catch (Exception e) {
////            response.setStatusCode(500);
////            response.setMessage("Error getting all users " + e.getMessage());
////
////        }
////        return response;
////    }
////
////    public UserResponse getUSerBookingHistory(String userId) {
////        UserResponse response = new UserResponse();
////
////        try {
////            User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(()-> new OurException("User Not Found"));
////            UserDTO userDTO = Utils.mapUserEntityToUserDTOPlusUserBookingsAndRoom(user);
////
////            response.setMessage("successful");
////            response.setStatusCode(200);
////            response.setUser(userDTO);
////
////        } catch (OurException e) {
////            response.setStatusCode(404);
////            response.setMessage(e.getMessage());
////
////        } catch (Exception e) {
////            response.setStatusCode(500);
////            response.setMessage("Error getting user bookings in " + e.getMessage());
////
////        }
////        return response;
////    }
////
////    public UserResponse deleteUser(String userId) {
////        UserResponse response = new UserResponse();
////
////        try {
////            userRepository.findById(Long.valueOf(userId)).orElseThrow(()-> new OurException("User Not Found"));
////            userRepository.deleteById(Long.valueOf(userId));
////
////            response.setMessage("successful");
////            response.setStatusCode(200);
////
////        } catch (OurException e) {
////            response.setStatusCode(404);
////            response.setMessage(e.getMessage());
////
////        } catch (Exception e) {
////            response.setStatusCode(500);
////            response.setMessage("Error deleting a user " + e.getMessage());
////
////        }
////        return response;
////    }
////
////    public UserResponse getUserById(String userId) {
////        UserResponse response = new UserResponse();
////
////        try {
////            User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(()-> new OurException("User Not Found"));
////            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);
////
////            response.setMessage("successful");
////            response.setStatusCode(200);
////            response.setUser(userDTO);
////
////        } catch (OurException e) {
////            response.setStatusCode(404);
////            response.setMessage(e.getMessage());
////
////        } catch (Exception e) {
////            response.setStatusCode(500);
////            response.setMessage("Error getting a user by id " + e.getMessage());
////
////        }
////        return response;
////    }
////
////    public UserResponse getMyInfo(String email) {
////        UserResponse response = new UserResponse();
////
////        try {
////            User user = userRepository.findByEmail(email).orElseThrow(()-> new OurException("User Not Found"));
////            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);
////
////            response.setMessage("successful");
////            response.setStatusCode(200);
////            response.setUser(userDTO);
////
////        } catch (OurException e) {
////            response.setStatusCode(404);
////            response.setMessage(e.getMessage());
////
////        } catch (Exception e) {
////            response.setStatusCode(500);
////            response.setMessage("Error getting a user info " + e.getMessage());
////
////        }
////        return response;
////    }
////
////
//
//
//
//     private final UserMapper userMapper;
//
//    public List<UserResponse> finAllUsersExceptSelf(Authentication connectedUser) {
//        return userRepository.findAllUsersExceptSelf(connectedUser.getName())
//                .stream()
//                .map(userMapper::toUserResponse)
//                .toList();
//    }
//}
