package com.badminton.exception;

public class AttendanceException extends RuntimeException {

    public AttendanceException(String message) {
        super(message);
    }

    public AttendanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
