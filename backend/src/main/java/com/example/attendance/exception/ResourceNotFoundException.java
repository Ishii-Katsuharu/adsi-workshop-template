package com.example.attendance.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " not found with id: " + id);
    }
}
