package com.nova.poneglyph.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(resourceName + " not found with identifier: " + identifier);
    }
}
