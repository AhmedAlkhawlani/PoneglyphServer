//
//package com.nova.poneglyph.config.exception;
//
//import com.nova.poneglyph.exception.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
//
//    @ExceptionHandler(OtpValidationException.class)
//    public ResponseEntity<Map<String, String>> handleOtp(OtpValidationException ex) {
//        return badRequest("otp_invalid", ex.getMessage());
//    }
//
//    @ExceptionHandler({TokenRefreshException.class, TokenReuseException.class})
//    public ResponseEntity<Map<String, String>> handleToken(RuntimeException ex) {
//        return unauthorized("token_error", ex.getMessage());
//    }
//
//    @ExceptionHandler(RateLimitExceededException.class)
//    public ResponseEntity<Map<String, String>> handleRateLimit(RateLimitExceededException ex) {
//        return status(HttpStatus.TOO_MANY_REQUESTS, "rate_limited", ex.getMessage());
//    }
//
//    @ExceptionHandler(AuthorizationException.class)
//    public ResponseEntity<Map<String, String>> handleAuthz(AuthorizationException ex) {
//        return status(HttpStatus.FORBIDDEN, "forbidden", ex.getMessage());
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
//        return badRequest("validation_failed", ex.getBindingResult().toString());
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, String>> handleAny(Exception ex) {
//        log.error("Unhandled exception:", ex);
//        return status(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", "Internal server error");
//    }
//
//    private ResponseEntity<Map<String, String>> badRequest(String code, String message) {
//        Map<String, String> body = new HashMap<>();
//        body.put("error", code);
//        body.put("message", message);
//        return ResponseEntity.badRequest().body(body);
//    }
//
//    private ResponseEntity<Map<String, String>> unauthorized(String code, String message) {
//        Map<String, String> body = new HashMap<>();
//        body.put("error", code);
//        body.put("message", message);
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
//    }
//
//    private ResponseEntity<Map<String, String>> status(HttpStatus status, String code, String message) {
//        Map<String, String> body = new HashMap<>();
//        body.put("error", code);
//        body.put("message", message);
//        return ResponseEntity.status(status).body(body);
//    }
//}
