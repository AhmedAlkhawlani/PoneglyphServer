package com.nova.poneglyph.exception;

// UserNotAuthorizedException.java
public class UserNotAuthorizedException extends RuntimeException {
    public UserNotAuthorizedException(String message) {
        super(message);
    }
}
