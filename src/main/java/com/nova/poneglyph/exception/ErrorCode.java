package com.nova.poneglyph.exception;

public enum ErrorCode {
    // Authentication errors
    INVALID_CREDENTIALS,
    USERNAME_ALREADY_EXISTS,
    EMAIL_ALREADY_EXISTS,

    // User errors
    USER_NOT_FOUND,
    INVALID_PASSWORD,

    // Chat errors
    CHAT_NOT_FOUND,
    CHAT_PARTICIPANT_NOT_FOUND,

    // Message errors
    MESSAGE_NOT_FOUND,

    // General errors
    INTERNAL_SERVER_ERROR,
    VALIDATION_ERROR,
    ACCESS_DENIED
}
