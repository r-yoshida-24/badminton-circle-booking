package com.badminton.controller;

import com.badminton.exception.DuplicateFieldException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateField(DuplicateFieldException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of(
                "error", "duplicate_field",
                "field", exception.getField(),
                "message", exception.getMessage()
            ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "error", "validation_error",
                "fields", fields
            ));
    }
}
