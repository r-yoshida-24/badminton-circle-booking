package com.badminton.exception;

public class LineAuthenticationException extends RuntimeException {

    public LineAuthenticationException(String message) {
        super(message);
    }

    public LineAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
