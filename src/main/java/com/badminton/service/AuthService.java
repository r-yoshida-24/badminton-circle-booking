package com.badminton.service;

import com.badminton.entity.User;
import com.badminton.entity.UserRole;
import com.badminton.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerParticipant(String username, String rawPassword, String email, String displayName) {
        return register(username, rawPassword, email, displayName, UserRole.PARTICIPANT);
    }

    public User registerAdmin(String username, String rawPassword, String email, String displayName) {
        return register(username, rawPassword, email, displayName, UserRole.ADMIN);
    }

    private User register(String username, String rawPassword, String email, String displayName, UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }
}
