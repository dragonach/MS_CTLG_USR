package com.example.demo.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username already exists: " + username);
    }

    public UsernameAlreadyExistsException(String username, Throwable cause) {
        super("Username already exists: " + username, cause);
    }
}