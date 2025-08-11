package com.nova.poneglyph.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(HttpStatus.NOT_FOUND, ErrorCode.valueOf(resourceName.toUpperCase() + "_NOT_FOUND"),
                String.format("%s with id %d not found", resourceName, id));
    }
}
