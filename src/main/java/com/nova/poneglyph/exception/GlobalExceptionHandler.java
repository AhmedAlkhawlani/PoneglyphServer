package com.nova.poneglyph.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. معالجة أخطاء التحقق من صحة بيانات الإدخال (DTO Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", "VALIDATION_FAILED");
        responseBody.put("message", "One or more field validations failed");
        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
        responseBody.put("details", errors); // تضمين تفاصيل الأخطاء لكل حقل

        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    // 2. معالجة الاستثناءات الخاصة بالمصادر غير الموجودة
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                "NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 3. معالجة أخطاء المصادقة (Authentication)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse error = new ErrorResponse(
                "AUTHENTICATION_FAILED",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 4. معالجة أخطاء التفويض (Authorization)
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(AuthorizationException ex) {
        ErrorResponse error = new ErrorResponse(
                "ACCESS_DENIED",
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // 5. معالجة أخطاء حد المعدل (Rate Limiting)
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RateLimitExceededException ex) {
        ErrorResponse error = new ErrorResponse(
                "RATE_LIMIT_EXCEEDED",
                ex.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS.value()
        );
        return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
    }

    // 6. معالجة أخطاء OTP
    @ExceptionHandler(OtpValidationException.class)
    public ResponseEntity<ErrorResponse> handleOtpValidationException(OtpValidationException ex) {
        ErrorResponse error = new ErrorResponse(
                "OTP_INVALID",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 7. معالجة أخطاء Token (مجمعة في معالج واحد)
    @ExceptionHandler({TokenRefreshException.class, TokenReuseException.class})
    public ResponseEntity<ErrorResponse> handleTokenExceptions(RuntimeException ex) {
        // يمكنك التمييز بينهما داخل المعالج إذا كان ذلك ضرورياً
        String errorCode = (ex instanceof TokenRefreshException) ? "TOKEN_REFRESH_FAILED" : "TOKEN_REUSE_DETECTED";

        ErrorResponse error = new ErrorResponse(
                errorCode,
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // 8. المعالج العام للاستثناءات غير المتوقعة
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // تسجيل الاستثناء بالكامل لتفادي فقدان معلومات التتبع (stack trace) المهمة
        log.error("Unhandled exception occurred: ", ex);

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal server error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


// Add this method to your GlobalExceptionHandler class

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        log.warn("Optimistic locking failure: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "CONCURRENT_MODIFICATION",
                "The resource was modified by another request. Please try again."
       ,HttpStatus.CONFLICT.value() );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex) {
        log.warn("User exception: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "USER_ERROR",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );

        // استخدام NOT_FOUND للخطأ "Profile not found"
        if (ex.getMessage().contains("not found")) {
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    // سجل (Record) لتهيئة استجابة الخطأ بشكل متسق
    public record ErrorResponse(String code, String message, int status) {}
}
