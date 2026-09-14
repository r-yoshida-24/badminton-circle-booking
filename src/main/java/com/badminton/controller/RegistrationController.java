package com.badminton.controller;

import com.badminton.dto.RegistrationRequest;
import com.badminton.entity.User;
import com.badminton.service.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RegistrationController {
    private final UserRegistrationService userRegistrationService;

    public RegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @GetMapping("/signup")
    public Map<String, String> signupInfo() {
        return Map.of("message", "Use POST /signup to register as a participant");
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody RegistrationRequest request) {
        User user = userRegistrationService.registerParticipant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole()
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/admins")
    public ResponseEntity<Map<String, Object>> createAdmin(@Valid @RequestBody RegistrationRequest request) {
        User user = userRegistrationService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole()
        ));
    }
}
