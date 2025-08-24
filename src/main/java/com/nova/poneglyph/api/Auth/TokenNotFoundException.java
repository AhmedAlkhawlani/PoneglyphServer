package com.nova.poneglyph.api.Auth;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}

