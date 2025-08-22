package com.nova.poneglyph.exception;

public class TokenReuseException extends RuntimeException {
    public TokenReuseException(String message) {
        super(message);
    }
}
