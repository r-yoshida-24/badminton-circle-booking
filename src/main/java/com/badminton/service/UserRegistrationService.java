package com.badminton.service;

import com.badminton.dto.RegistrationRequest;
import com.badminton.entity.User;
import com.badminton.exception.DuplicateFieldException;
import com.badminton.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationService {
    private static final String ROLE_PARTICIPANT = "PARTICIPANT";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerParticipant(RegistrationRequest request) {
        return registerWithRole(request, ROLE_PARTICIPANT);
    }

    @Transactional
    public User registerAdmin(RegistrationRequest request) {
        return registerWithRole(request, ROLE_ADMIN);
    }

    private User registerWithRole(RegistrationRequest request, String role) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateFieldException("username", "Username is already in use");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateFieldException("email", "Email is already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }
}
